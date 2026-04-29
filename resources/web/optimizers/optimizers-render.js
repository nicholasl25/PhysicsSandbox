/**
 * Optimizer landscape demo — heatmap, trajectories, world/canvas mapping.
 * Depends: (none)
 * Exposes: window.OptimizerRender
 */
(function () {
    'use strict';

    let gridCache = { key: '', w: 0, h: 0, minL: 0, maxL: 0 };

    function invalidateGrid() {
        gridCache.key = '';
    }

    function worldToCanvas(x, y, w, h, b) {
        const px = (x - b.xmin) / (b.xmax - b.xmin) * w;
        const py = h - (y - b.ymin) / (b.ymax - b.ymin) * h;
        return [px, py];
    }

    function canvasToWorld(px, py, w, h, b) {
        const x = b.xmin + (px / w) * (b.xmax - b.xmin);
        const y = b.ymin + ((h - py) / h) * (b.ymax - b.ymin);
        return [x, y];
    }

    function lerpColor(t, c0, c1) {
        const r = Math.round(c0[0] + (c1[0] - c0[0]) * t);
        const gch = Math.round(c0[1] + (c1[1] - c0[1]) * t);
        const bch = Math.round(c0[2] + (c1[2] - c0[2]) * t);
        return `rgb(${r},${gch},${bch})`;
    }

    function heatColor(t) {
        const stops = [
            [13, 27, 42],
            [30, 58, 95],
            [65, 105, 225],
            [34, 197, 94],
            [252, 211, 77],
            [251, 146, 60],
            [239, 68, 68],
        ];
        const n = stops.length - 1;
        const u = Math.max(0, Math.min(1, t)) * n;
        const i = Math.floor(u);
        const f = u - i;
        return lerpColor(f, stops[i], stops[Math.min(i + 1, n)]);
    }

    function buildGrid(w, h, landKey, getLand) {
        const L = getLand();
        const b = L.bounds;
        const key = landKey + '|' + w + 'x' + h;
        if (gridCache.key === key && gridCache.w === w && gridCache.h === h) return;

        let minL = Infinity;
        let maxL = -Infinity;
        const nx = Math.min(280, Math.max(80, Math.floor(w / 3)));
        const ny = Math.min(280, Math.max(80, Math.floor(h / 3)));
        const cellW = w / nx;
        const cellH = h / ny;
        const samples = [];

        for (let j = 0; j < ny; j++) {
            for (let i = 0; i < nx; i++) {
                const cx = (i + 0.5) / nx;
                const cy = (j + 0.5) / ny;
                const wx = b.xmin + cx * (b.xmax - b.xmin);
                const wy = b.ymin + cy * (b.ymax - b.ymin);
                const lv = L.loss(wx, wy);
                samples.push(lv);
                if (lv < minL) minL = lv;
                if (lv > maxL) maxL = lv;
            }
        }
        if (minL === maxL) maxL = minL + 1e-6;

        gridCache = { key, w, h, minL, maxL, nx, ny, cellW, cellH, samples, bounds: b };
    }

    function drawHeatmap(ctx, w, h, landKey, getLand) {
        buildGrid(w, h, landKey, getLand);
        const g = gridCache;
        const logMin = Math.log(g.minL + 1e-9);
        const logMax = Math.log(g.maxL + 1e-9);
        const span = logMax - logMin || 1;

        let idx = 0;
        for (let j = 0; j < g.ny; j++) {
            for (let i = 0; i < g.nx; i++) {
                const l = g.samples[idx++];
                const t = (Math.log(l + 1e-9) - logMin) / span;
                ctx.fillStyle = heatColor(t);
                ctx.fillRect(i * g.cellW, j * g.cellH, g.cellW + 1, g.cellH + 1);
            }
        }
    }

    /**
     * @param {HTMLCanvasElement} canvas
     * @param {object} opts
     * @param {string} opts.landKey
     * @param {function(): object} opts.getLand
     * @param {Array<{ trail: number[][], theta: number[], color: string, convergedStep: number|null }>} opts.runners
     */
    function draw(canvas, opts) {
        const ctx = canvas.getContext('2d');
        const dpr = Math.min(2, window.devicePixelRatio || 1);
        const rect = canvas.getBoundingClientRect();
        const w = Math.floor(rect.width * dpr);
        const h = Math.floor(rect.height * dpr);
        if (canvas.width !== w || canvas.height !== h) {
            canvas.width = w;
            canvas.height = h;
        }
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.clearRect(0, 0, w, h);

        drawHeatmap(ctx, w, h, opts.landKey, opts.getLand);

        const L = opts.getLand();
        const b = L.bounds;
        const runners = opts.runners || [];

        for (let r = 0; r < runners.length; r++) {
            const runner = runners[r];
            if (runner.trail.length > 1) {
                ctx.strokeStyle = runner.color;
                ctx.globalAlpha = 0.88;
                ctx.lineWidth = 2 * dpr;
                ctx.lineJoin = 'round';
                ctx.beginPath();
                for (let i = 0; i < runner.trail.length; i++) {
                    const [px, py] = worldToCanvas(runner.trail[i][0], runner.trail[i][1], w, h, b);
                    if (i === 0) ctx.moveTo(px, py);
                    else ctx.lineTo(px, py);
                }
                ctx.stroke();
                ctx.globalAlpha = 1;
            }

            const [cx, cy] = worldToCanvas(runner.theta[0], runner.theta[1], w, h, b);
            ctx.fillStyle = runner.color;
            ctx.strokeStyle = '#0a0a0a';
            ctx.lineWidth = 1.75 * dpr;
            const rad = runner.convergedStep !== null ? 5.5 * dpr : 6.5 * dpr;
            ctx.beginPath();
            ctx.arc(cx, cy, rad, 0, Math.PI * 2);
            ctx.fill();
            ctx.stroke();
        }
    }

    window.OptimizerRender = {
        draw,
        invalidateGrid,
        worldToCanvas,
        canvasToWorld,
    };
}());
