/**
 * Optimizer landscape demo — DOM wiring, run loop, multi-runner compare state.
 * Depends: OptimizerLandscapes, OptimizerCore, OptimizerRender
 */
(function () {
    'use strict';

    const landscapes = window.OptimizerLandscapes;
    const Core = window.OptimizerCore;
    const Render = window.OptimizerRender;

    if (!landscapes || !Core || !Render) {
        throw new Error('OptimizerApp: missing OptimizerLandscapes, OptimizerCore, or OptimizerRender');
    }

    const KIND_ORDER = ['gd', 'sgd', 'sgdm', 'adagrad', 'rmsprop', 'adam'];
    const KIND_LABEL = {
        gd: 'GD',
        sgd: 'SGD',
        sgdm: 'SGD + m',
        adagrad: 'Adagrad',
        rmsprop: 'RMSProp',
        adam: 'Adam',
    };
    const KIND_COLOR = {
        gd: '#22d3ee',
        sgd: '#c084fc',
        sgdm: '#f472b6',
        adagrad: '#fbbf24',
        rmsprop: '#4ade80',
        adam: '#fb923c',
    };

    const DEFAULT_HINT = 'Click the plot to set θ₀ for all enabled optimizers.';

    const canvas = document.getElementById('opt-canvas');
    const globalStatusEl = document.getElementById('global-status');
    const roundsEl = document.getElementById('opt-rounds-display');

    const ui = {
        surface: document.getElementById('surface'),
        tol: document.getElementById('tol'),
        maxiter: document.getElementById('maxiter'),
        runspeed: document.getElementById('runspeed'),
        eps: document.getElementById('eps'),
    };

    const app = {
        landKey: 'bowl',
        runners: [],
        sharedStart: [0, 0],
        roundCount: 0,
        running: false,
        rafId: 0,
        ui,
        canvas,
        getLand() {
            return landscapes[this.landKey];
        },
        getParams(kind) {
            return getParamsForKind(kind);
        },
    };

    function getParamsForKind(kind) {
        const root = document.querySelector(`.opt-method[data-kind="${kind}"]`);
        const out = {
            lr: 0.05,
            eps: parseFloat(ui.eps && ui.eps.value) || 1e-8,
            batch: 4,
            momentum: 0.9,
            rho: 0.9,
            b1: 0.9,
            b2: 0.999,
        };
        if (!Number.isFinite(out.eps) || out.eps <= 0) out.eps = 1e-8;
        if (!root) return out;

        const lrEl = root.querySelector('[data-field="lr"]');
        const lr = parseFloat(lrEl && lrEl.value);
        if (Number.isFinite(lr)) out.lr = lr;

        if (kind === 'sgd' || kind === 'sgdm') {
            const bEl = root.querySelector('[data-field="batch"]');
            const b = parseInt(bEl && bEl.value, 10);
            out.batch = Number.isFinite(b) ? Math.max(1, b) : 4;
        }
        if (kind === 'sgdm') {
            const mEl = root.querySelector('[data-field="momentum"]');
            const mu = parseFloat(mEl && mEl.value);
            out.momentum = Number.isFinite(mu) ? mu : 0.9;
        }
        if (kind === 'rmsprop') {
            const rEl = root.querySelector('[data-field="rho"]');
            const rho = parseFloat(rEl && rEl.value);
            out.rho = Number.isFinite(rho) ? rho : 0.9;
        }
        if (kind === 'adam') {
            const b1El = root.querySelector('[data-field="b1"]');
            const b2El = root.querySelector('[data-field="b2"]');
            const b1 = parseFloat(b1El && b1El.value);
            const b2 = parseFloat(b2El && b2El.value);
            if (Number.isFinite(b1)) out.b1 = b1;
            if (Number.isFinite(b2)) out.b2 = b2;
        }
        return out;
    }

    function getSelectedKinds() {
        const kinds = [];
        KIND_ORDER.forEach((k) => {
            const box = document.querySelector(`.opt-enable[data-kind="${k}"]`);
            if (box && box.checked) kinds.push(k);
        });
        return kinds;
    }

    function ensureAtLeastOneKind() {
        if (getSelectedKinds().length === 0) {
            const gd = document.querySelector('.opt-enable[data-kind="gd"]');
            if (gd) gd.checked = true;
        }
    }

    function syncAllLearningRates(valueStr) {
        document.querySelectorAll('.opt-method [data-field="lr"]').forEach((el) => {
            el.value = valueStr;
        });
    }

    function rebuildRunners() {
        ensureAtLeastOneKind();
        const kinds = getSelectedKinds();
        const L = app.getLand();
        const t0 = app.sharedStart.slice();
        app.runners = kinds.map((k) => ({
            kind: k,
            label: KIND_LABEL[k] || k,
            color: KIND_COLOR[k] || '#94a3b8',
            theta: t0.slice(),
            trail: [[t0[0], t0[1]]],
            optState: Core.resetOptimizerState(),
            steps: 0,
            convergedStep: null,
        }));
        app.roundCount = 0;
    }

    function setGlobalHint(msg) {
        if (globalStatusEl) globalStatusEl.textContent = msg || DEFAULT_HINT;
    }

    function draw() {
        Render.draw(canvas, {
            landKey: app.landKey,
            runners: app.runners,
            getLand: app.getLand.bind(app),
        });
    }

    function updateStatus() {
        const L = app.getLand();
        KIND_ORDER.forEach((kind) => {
            const el = document.querySelector(`[data-metrics="${kind}"]`);
            if (!el) return;
            const enabled = document.querySelector(`.opt-enable[data-kind="${kind}"]`);
            if (!enabled || !enabled.checked) {
                el.textContent = '—';
                return;
            }
            const runner = app.runners.find((r) => r.kind === kind);
            if (!runner) {
                el.textContent = '—';
                return;
            }
            const Lv = L.loss(runner.theta[0], runner.theta[1]);
            const [gx, gy] = L.grad(runner.theta[0], runner.theta[1]);
            const gn = Math.hypot(gx, gy);
            let s = `steps ${runner.steps} · L ${Lv.toExponential(2)} · ‖g‖ ${gn.toExponential(2)}`;
            if (runner.convergedStep !== null) {
                s += ` · ✓@${runner.convergedStep}`;
            }
            el.textContent = s;
        });
        if (roundsEl) roundsEl.textContent = `Rounds: ${app.roundCount}`;
    }

    function pauseRun() {
        app.running = false;
        if (app.rafId) cancelAnimationFrame(app.rafId);
        app.rafId = 0;
    }

    function allConverged() {
        return app.runners.length > 0 && app.runners.every((r) => r.convergedStep !== null);
    }

    function stepAllRunners() {
        const tol = parseFloat(ui.tol.value);
        let diverged = false;
        let divergedLabel = '';
        let stepped = false;

        for (let i = 0; i < app.runners.length; i++) {
            const r = app.runners[i];
            if (r.convergedStep !== null) continue;
            stepped = true;
            const gnorm = Core.stepRunner(r, app);
            if (!Number.isFinite(r.theta[0]) || !Number.isFinite(r.theta[1]) || !Number.isFinite(gnorm)) {
                diverged = true;
                divergedLabel = r.label;
                break;
            }
            if (gnorm < tol) {
                r.convergedStep = r.steps;
            }
        }

        if (stepped) app.roundCount += 1;

        return {
            diverged,
            divergedLabel,
            allConv: app.runners.length > 0 && app.runners.every((r) => r.convergedStep !== null),
        };
    }

    function onLandscapeChange() {
        pauseRun();
        app.landKey = ui.surface.value;
        const L = app.getLand();
        app.sharedStart = L.defaultStart.slice();
        syncAllLearningRates(String(L.defaultLr));
        Render.invalidateGrid();
        rebuildRunners();
        draw();
        updateStatus();
        setGlobalHint(DEFAULT_HINT);
    }

    function resetPath() {
        pauseRun();
        const L = app.getLand();
        app.sharedStart = L.defaultStart.slice();
        rebuildRunners();
        draw();
        updateStatus();
        setGlobalHint(DEFAULT_HINT);
    }

    function runFrame() {
        if (!app.running) return;
        const maxSteps = parseInt(ui.maxiter.value, 10) || 8000;
        const perFrame = parseInt(ui.runspeed.value, 10) || 4;

        for (let k = 0; k < perFrame; k++) {
            if (app.roundCount >= maxSteps) {
                pauseRun();
                updateStatus();
                setGlobalHint('Stopped: max rounds (see Advanced).');
                draw();
                return;
            }
            if (allConverged()) {
                pauseRun();
                updateStatus();
                setGlobalHint('All enabled optimizers converged (‖∇L‖ < tol).');
                draw();
                return;
            }

            const { diverged, divergedLabel, allConv } = stepAllRunners();
            if (diverged) {
                pauseRun();
                setGlobalHint(`Diverged (${divergedLabel}) — lower η or reset.`);
                draw();
                updateStatus();
                return;
            }
            if (allConv) {
                pauseRun();
                updateStatus();
                setGlobalHint('All enabled optimizers converged (‖∇L‖ < tol).');
                draw();
                return;
            }
        }
        updateStatus();
        draw();
        app.rafId = requestAnimationFrame(runFrame);
    }

    function setupAdvancedCollapsible() {
        const header = document.getElementById('optimizer-advanced-header');
        const content = document.getElementById('optimizer-advanced-content');
        const label = document.getElementById('optimizer-advanced-label');
        if (!header || !content || !label) return;

        function toggle() {
            const expanded = content.classList.contains('expanded');
            if (expanded) {
                content.classList.remove('expanded');
                label.textContent = '▶ Advanced Settings';
            } else {
                content.classList.add('expanded');
                label.textContent = '▼ Advanced Settings';
            }
        }

        header.addEventListener('click', toggle);
        header.addEventListener('keydown', (ev) => {
            if (ev.key === 'Enter' || ev.key === ' ') {
                ev.preventDefault();
                toggle();
            }
        });
    }

    function setupMethodCollapsibles() {
        document.querySelectorAll('.opt-method-toggle').forEach((btn) => {
            btn.addEventListener('click', () => {
                const kind = btn.getAttribute('data-toggle');
                const section = document.querySelector(`.opt-method[data-kind="${kind}"]`);
                if (!section) return;
                const panel = section.querySelector('.opt-method-panel');
                const icon = btn.querySelector('.opt-method-toggle-icon');
                if (!panel || !icon) return;
                const open = panel.hasAttribute('hidden');
                if (open) {
                    panel.removeAttribute('hidden');
                    icon.textContent = '▼';
                    btn.setAttribute('aria-expanded', 'true');
                } else {
                    panel.setAttribute('hidden', '');
                    icon.textContent = '▶';
                    btn.setAttribute('aria-expanded', 'false');
                }
            });
        });
    }

    function wireOptimizerEnables() {
        document.querySelectorAll('.opt-enable').forEach((el) => {
            el.addEventListener('change', () => {
                pauseRun();
                ensureAtLeastOneKind();
                rebuildRunners();
                draw();
                updateStatus();
                setGlobalHint(DEFAULT_HINT);
            });
        });
    }

    function init() {
        setupAdvancedCollapsible();
        setupMethodCollapsibles();
        wireOptimizerEnables();

        if (ui.eps) {
            ui.eps.addEventListener('change', () => {
                draw();
            });
        }

        document.getElementById('btn-step').addEventListener('click', () => {
            pauseRun();
            const { diverged, divergedLabel, allConv } = stepAllRunners();
            if (diverged) {
                setGlobalHint(`Diverged (${divergedLabel}) — lower η or reset.`);
            } else {
                updateStatus();
                if (allConv) {
                    setGlobalHint('All enabled optimizers converged (‖∇L‖ < tol).');
                } else {
                    setGlobalHint(DEFAULT_HINT);
                }
            }
            draw();
        });

        document.getElementById('btn-run').addEventListener('click', () => {
            if (app.running) return;
            app.running = true;
            setGlobalHint('Running…');
            app.rafId = requestAnimationFrame(runFrame);
        });

        document.getElementById('btn-pause').addEventListener('click', pauseRun);

        document.getElementById('btn-reset').addEventListener('click', () => {
            resetPath();
        });

        ui.surface.addEventListener('change', () => {
            onLandscapeChange();
        });

        canvas.addEventListener('click', (ev) => {
            pauseRun();
            const rect = canvas.getBoundingClientRect();
            const dpr = canvas.width / rect.width;
            const px = (ev.clientX - rect.left) * dpr;
            const py = (ev.clientY - rect.top) * dpr;
            const L = app.getLand();
            const [wx, wy] = Render.canvasToWorld(px, py, canvas.width, canvas.height, L.bounds);
            app.sharedStart = [wx, wy];
            rebuildRunners();
            updateStatus();
            draw();
            setGlobalHint(DEFAULT_HINT);
        });

        window.addEventListener('resize', () => {
            Render.invalidateGrid();
            draw();
        });

        onLandscapeChange();
    }

    init();
}());
