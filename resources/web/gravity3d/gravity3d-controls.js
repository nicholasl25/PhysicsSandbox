/**
 * Gravity3D Controls - UI for Add/Inspect/Settings and simulation wiring.
 * Depends: Gravity3DSimulation (gravity3d-core, gravity3d-physics, gravity3d-render).
 */

let simulation = null;

// Mass: value in these units → kg (ordered low to high)
const MASS_TO_KG = {
    kg: 1,
    Moon: 7.342e22,
    Earth: 5.9722e24,
    Jupiter: 1.8982e27,
    Sun: 1.9885e30
};

// Radius: value in these units → m (ordered low to high)
const RADIUS_TO_M = {
    m: 1,
    mi: 1609.344,
    Moon: 1.7374e6,
    Earth: 6.371e6,
    Jupiter: 6.9911e7,
    Sun: 6.957e8,
    au: 1.495978707e11
};

// Initialize when page loads
document.addEventListener('DOMContentLoaded', () => {
    if (typeof THREE === 'undefined') {
        console.error('Three.js not loaded yet, waiting...');
        setTimeout(() => {
            if (typeof THREE === 'undefined') {
                alert('Error: Three.js library failed to load. Please refresh the page.');
                return;
            }
            initSimulation();
        }, 500);
    } else {
        initSimulation();
    }
});

function initSimulation() {
    try {
        simulation = new Gravity3DSimulation('canvas-container');
        setupTabs();
        setupControls();
    } catch (error) {
        console.error('Failed to initialize simulation:', error);
        alert('Error initializing simulation: ' + error.message);
    }
}

function setupTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            const targetTab = button.dataset.tab;
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            tabContents.forEach(content => content.classList.remove('active'));
            const targetPanel = document.getElementById(`${targetTab}-panel`);
            if (targetPanel) {
                targetPanel.classList.add('active');
                if (targetTab === 'inspect') refreshInspectPanel();
            }
        });
    });

    setInterval(() => {
        const inspectPanel = document.getElementById('inspect-panel');
        if (inspectPanel && inspectPanel.classList.contains('active')) {
            refreshInspectPanel();
        }
    }, 500);
}

function setupControls() {
    document.getElementById('add-planet-button').addEventListener('click', () => {
        const planetData = getPlanetData();
        if (planetData) {
            simulation.addPlanet(planetData);
        } else {
            alert('Please enter valid numbers!');
        }
    });

    document.getElementById('clear-button').addEventListener('click', () => {
        if (confirm('Are you sure you want to clear the simulation?')) {
            simulation.clearSimulation();
        }
    });

    const gravitySlider = document.getElementById('gravity-slider');
    const gravityValue = document.getElementById('gravity-value');

    const MIN_MULT = 0.01; // 0.01x
    const MAX_MULT = 100;  // 100x
    const LOG_MIN_EXP = Math.log10(MIN_MULT);
    const LOG_MAX_EXP = Math.log10(MAX_MULT);

    function sliderValueToMultiplier(sliderEl) {
        const min = parseFloat(sliderEl.min ?? 0);
        const max = parseFloat(sliderEl.max ?? 1);
        const v = parseFloat(sliderEl.value);
        const t = (v - min) / ((max - min) || 1); // 0..1
        const exp = LOG_MIN_EXP + t * (LOG_MAX_EXP - LOG_MIN_EXP); // -2..2
        return Math.pow(10, exp); // multiplier
    }

    function formatMultiplier(mult) {
        if (!isFinite(mult) || mult <= 0) return '—';
        return `${mult.toFixed(2)}x`;
    }

    function formatHundredth(n) {
        if (!isFinite(n)) return '—';
        const rounded = Math.round(n * 100) / 100;
        return rounded.toFixed(2);
    }

    const speedOfLightSlider = document.getElementById('speed-of-light-slider');
    const speedOfLightValue = document.getElementById('speed-of-light-value');
    function applyCFromSlider() {
        const mult = sliderValueToMultiplier(speedOfLightSlider); // 0.01..100
        speedOfLightValue.textContent = formatMultiplier(mult);
        // setC expects "pct" where pct/100 = multiplier.
        simulation.setC(mult * 100);
    }
    speedOfLightSlider.addEventListener('input', applyCFromSlider);

    const sigmaSlider = document.getElementById('sigma-slider');
    const sigmaValue = document.getElementById('sigma-value');
    function applySigmaFromSlider() {
        const mult = sliderValueToMultiplier(sigmaSlider); // 0.01..100
        sigmaValue.textContent = formatMultiplier(mult);
        // setSigma expects "pct" where pct/100 = multiplier.
        simulation.setSigma(mult * 100);
    }
    sigmaSlider.addEventListener('input', applySigmaFromSlider);

    // Initialize displays + simulation constants from the default slider values.
    applyCFromSlider();
    applySigmaFromSlider();

    function applyGFromSlider() {
        const mult = sliderValueToMultiplier(gravitySlider); // 0.01..100
        gravityValue.textContent = formatMultiplier(mult);
        // setG expects "pct" where pct/100 = multiplier.
        simulation.setG(mult * 100);
    }
    gravitySlider.addEventListener('input', applyGFromSlider);
    applyGFromSlider();

    const timeFactorSlider = document.getElementById('timefactor-slider');
    const timeFactorValue = document.getElementById('timefactor-value');
    function applyTimeFromSlider() {
        // Use the same log mapping as c/sigma sliders; here we interpret the multiplier as days/s.
        const daysPerSecond = sliderValueToMultiplier(timeFactorSlider); // 0.01..100 (days/s)
        timeFactorValue.textContent = formatHundredth(daysPerSecond);
        simulation.setTimeFactor(daysPerSecond * 86400);
    }
    timeFactorSlider.addEventListener('input', applyTimeFromSlider);
    applyTimeFromSlider();

    function applyCollisionModeFromUI() {
        const selected = document.querySelector('input[name="collision-mode"]:checked');
        simulation.setBounce(selected !== null && selected.value === 'bounce');
    }
    document.querySelectorAll('input[name="collision-mode"]').forEach((radio) => {
        radio.addEventListener('change', applyCollisionModeFromUI);
    });
    applyCollisionModeFromUI();

    setInterval(() => {
        const pauseText = document.getElementById('pause-text');
        if (simulation && simulation.isPaused) {
            pauseText.textContent = 'PAUSED - Press SPACE to resume';
            pauseText.style.color = '#ff6b6b';
        } else {
            pauseText.textContent = 'Press SPACE to pause';
            pauseText.style.color = '#888';
        }
    }, 100);
}

function formatNum(x) {
    if (x === undefined || x === null) return '—';
    const n = Number(x);
    if (isNaN(n)) return '—';
    if (Math.abs(n) >= 1e6 || (Math.abs(n) < 0.0001 && n !== 0)) return n.toExponential(3);
    return n.toFixed(4);
}

function isInspectFieldsEditing() {
    const fieldsEl = document.getElementById('inspect-fields');
    const ae = document.activeElement;
    return !!(fieldsEl && ae && fieldsEl.contains(ae) && (ae.tagName === 'INPUT' || ae.tagName === 'TEXTAREA'));
}

function rebuildPlanetMesh(planet) {
    simulation.disposePlanetMesh(planet);
    simulation.createPlanetMesh(planet);
    simulation.updatePlanetPositions();
}

/**
 * Parse "x, y, z" or "(x, y, z)" into a 3D Vector; returns null if invalid.
 */
function parseVector3Components(str) {
    const cleaned = String(str).trim().replace(/^\(/, '').replace(/\)$/, '');
    const parts = cleaned.split(/[,;\s]+/).map((s) => s.trim()).filter((s) => s.length > 0);
    if (parts.length < 3) return null;
    const x = parseFloat(parts[0]);
    const y = parseFloat(parts[1]);
    const z = parseFloat(parts[2]);
    if (![x, y, z].every((n) => Number.isFinite(n))) return null;
    return new Vector([x, y, z]);
}

/**
 * @param {Planet} planet
 * @param {string} fieldKey
 * @param {string} raw
 * @returns {boolean}
 */
function applyInspectFieldChange(planet, fieldKey, raw) {
    const state = planet.getState();
    const consts = simulation.consts;
    switch (fieldKey) {
        case 'name': {
            const name = String(raw).trim();
            planet.name = name.length ? name : state.getType();
            return true;
        }
        case 'mass': {
            const v = parseFloat(String(raw).replace(/,/g, ''));
            if (!Number.isFinite(v) || v <= 0) return false;
            planet.state = new State(v, state.getRadius(), state.getTemperature(), consts);
            rebuildPlanetMesh(planet);
            return true;
        }
        case 'radius': {
            const v = parseFloat(String(raw).replace(/,/g, ''));
            if (!Number.isFinite(v) || v <= 0) return false;
            planet.state = new State(state.getMass(), v, state.getTemperature(), consts);
            rebuildPlanetMesh(planet);
            return true;
        }
        case 'temperature': {
            const v = parseFloat(String(raw).replace(/,/g, ''));
            if (!Number.isFinite(v) || v < 0) return false;
            planet.state = new State(state.getMass(), state.getRadius(), v, consts);
            rebuildPlanetMesh(planet);
            return true;
        }
        case 'angularVelocity': {
            const v = parseFloat(String(raw).replace(/,/g, ''));
            if (!Number.isFinite(v)) return false;
            planet.angularVelocity = v;
            return true;
        }
        case 'position': {
            const vec = parseVector3Components(raw);
            if (!vec) return false;
            planet.setPosition(vec);
            simulation.updatePlanetPositions();
            return true;
        }
        case 'velocity': {
            const vec = parseVector3Components(raw);
            if (!vec) return false;
            planet.setVelocity(vec);
            return true;
        }
        default:
            return false;
    }
}

function inspectFieldEditInitialValue(planet, fieldKey) {
    const state = planet.getState();
    const pos = planet.getPosition();
    const vel = planet.getVelocity();
    switch (fieldKey) {
        case 'name':
            return planet.getName();
        case 'mass':
            return String(state.getMass());
        case 'radius':
            return String(state.getRadius());
        case 'temperature':
            return String(state.getTemperature());
        case 'angularVelocity':
            return String(planet.getAngularVelocity());
        case 'position':
            return `${pos.get(0)}, ${pos.get(1)}, ${pos.dimensions() > 2 ? pos.get(2) : 0}`;
        case 'velocity':
            return `${vel.get(0)}, ${vel.get(1)}, ${vel.dimensions() > 2 ? vel.get(2) : 0}`;
        default:
            return '';
    }
}

function beginInspectValueEdit(spanEl, planet, fieldKey) {
    if (spanEl.querySelector('input')) return;
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'inspect-inline-input';
    input.value = inspectFieldEditInitialValue(planet, fieldKey);
    input.dataset.fieldKey = fieldKey;

    let finished = false;

    function teardown(commit) {
        if (finished) return;
        input.removeEventListener('blur', onBlur);
        input.removeEventListener('keydown', onKeydown);
        if (!commit) {
            finished = true;
            refreshInspectPanel();
            return;
        }
        const ok = applyInspectFieldChange(planet, fieldKey, input.value);
        if (!ok) {
            alert('Invalid value for this field.');
            input.addEventListener('blur', onBlur);
            input.addEventListener('keydown', onKeydown);
            input.focus();
            input.select();
            return;
        }
        finished = true;
        refreshInspectPanel();
    }

    function onBlur() {
        teardown(true);
    }

    function onKeydown(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            teardown(true);
        } else if (e.key === 'Escape') {
            e.preventDefault();
            teardown(false);
        }
    }

    spanEl.textContent = '';
    spanEl.appendChild(input);
    input.addEventListener('blur', onBlur);
    input.addEventListener('keydown', onKeydown);
    requestAnimationFrame(() => {
        input.focus();
        input.select();
    });
}

function appendInspectFieldRow(fieldsEl, planet, label, displayValue, fieldKey, editable) {
    const row = document.createElement('div');
    row.className = 'row';
    const lab = document.createElement('span');
    lab.textContent = label;
    const val = document.createElement('span');
    val.className = editable ? 'inspect-value inspect-value-editable' : 'inspect-value';
    val.textContent = displayValue;
    if (editable) {
        val.title = 'Click to edit';
        val.addEventListener('click', (e) => {
            e.stopPropagation();
            beginInspectValueEdit(val, planet, fieldKey);
        });
    }
    row.appendChild(lab);
    row.appendChild(val);
    fieldsEl.appendChild(row);
}

function refreshInspectPanel() {
    if (!simulation) return;
    const detailEl = document.getElementById('inspect-detail');
    const listEl = document.getElementById('inspect-list');
    const selected = simulation.selectedPlanet;
    const planets = simulation.planets;

    if (planets.length === 0) {
        listEl.innerHTML = '<div class="inspect-list-empty">No objects in simulation</div>';
        detailEl.style.display = 'none';
        return;
    }

    listEl.innerHTML = '';
    planets.forEach((planet) => {
        const row = document.createElement('div');
        row.className = 'inspect-list-item';
        const name = document.createElement('span');
        name.className = 'name';
        name.textContent = planet.getName();
        const btnEye = document.createElement('button');
        btnEye.className = 'btn-icon btn-inspect';
        btnEye.title = 'Select';
        btnEye.textContent = '✓';
        btnEye.addEventListener('click', () => simulation.selectPlanet(planet));
        const btnDel = document.createElement('button');
        btnDel.className = 'btn-icon btn-delete';
        btnDel.title = 'Remove';
        btnDel.textContent = '×';
        btnDel.addEventListener('click', () => {
            if (confirm(`Remove ${planet.getName()}?`)) simulation.removePlanet(planet);
        });
        row.appendChild(name);
        row.appendChild(btnEye);
        row.appendChild(btnDel);
        listEl.appendChild(row);
    });

    if (selected) {
        detailEl.style.display = 'block';
        const editing = isInspectFieldsEditing();
        const state = selected.getState();
        const img = document.getElementById('inspect-image');
        const path = state.getTexturepath();
        const wrap = detailEl.querySelector('.inspect-image-wrap');
        if (!editing) {
            if (path) {
                img.src = path;
                img.alt = selected.getName();
                img.style.display = 'block';
                wrap.style.background = '#2a2a2a';
            } else {
                img.style.display = 'none';
                const c = state.getColor();
                wrap.style.background = c ? `rgb(${Math.round(255 * c.r)},${Math.round(255 * c.g)},${Math.round(255 * c.b)})` : '#2a2a2a';
            }
        }
        const pos = selected.getPosition();
        const vel = selected.getVelocity();
        const lumSol = state.getLuminosity();
        const fieldsEl = document.getElementById('inspect-fields');
        if (!editing) {
            fieldsEl.innerHTML = '';
            appendInspectFieldRow(fieldsEl, selected, 'Name', selected.getName(), 'name', true);
            appendInspectFieldRow(fieldsEl, selected, 'Type', state.getType(), null, false);
            appendInspectFieldRow(fieldsEl, selected, 'Mass (kg)', formatNum(state.getMass()), 'mass', true);
            appendInspectFieldRow(fieldsEl, selected, 'Radius (m)', formatNum(state.getRadius()), 'radius', true);
            appendInspectFieldRow(fieldsEl, selected, 'Temperature (K)', formatNum(state.getTemperature()), 'temperature', true);
            appendInspectFieldRow(fieldsEl, selected, 'Luminosity (L☉)', lumSol === null ? '—' : formatNum(lumSol), null, false);
            appendInspectFieldRow(fieldsEl, selected, 'Angular vel.', formatNum(selected.getAngularVelocity()), 'angularVelocity', true);
            appendInspectFieldRow(
                fieldsEl,
                selected,
                'Position (m)',
                `(${formatNum(pos.get(0))}, ${formatNum(pos.get(1))}, ${pos.dimensions() > 2 ? formatNum(pos.get(2)) : '0'})`,
                'position',
                true
            );
            appendInspectFieldRow(
                fieldsEl,
                selected,
                'Velocity (m/s)',
                `(${formatNum(vel.get(0))}, ${formatNum(vel.get(1))}, ${vel.dimensions() > 2 ? formatNum(vel.get(2)) : '0'})`,
                'velocity',
                true
            );
        }
    } else {
        detailEl.style.display = 'none';
    }
}

function getPlanetData() {
    try {
        const massNum = parseFloat(document.getElementById('mass-field').value);
        const massUnit = document.getElementById('mass-unit').value;
        const radiusNum = parseFloat(document.getElementById('radius-field').value);
        const radiusUnit = document.getElementById('radius-unit').value;
        const period = parseFloat(document.getElementById('period-field').value);
        const temperature = parseFloat(document.getElementById('temperature-field').value);
        const name = document.getElementById('name-field').value.trim();

        if (isNaN(massNum) || isNaN(radiusNum) || isNaN(period) || isNaN(temperature)) {
            return null;
        }
        const mass = massNum * MASS_TO_KG[massUnit];
        const radius = radiusNum * RADIUS_TO_M[radiusUnit];

        return {
            mass,
            radius,
            x: 0, y: 0, z: 0,
            vx: 0, vy: 0, vz: 0,
            period,
            temperature,
            texturePath: null,
            name
        };
    } catch (e) {
        return null;
    }
}

function toggleAdvanced() {
    const content = document.getElementById('advanced-content');
    const header = document.querySelector('.collapsible-header span');
    const isExpanded = content.classList.contains('expanded');

    if (isExpanded) {
        content.classList.remove('expanded');
        header.textContent = '▶ Advanced Settings';
    } else {
        content.classList.add('expanded');
        header.textContent = '▼ Advanced Settings';
    }
}
