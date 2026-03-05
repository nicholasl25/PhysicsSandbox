/**
 * UI Controller for Gravity3D Web Simulation
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
    // Wait a bit for Three.js to load if needed
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
        // Create simulation instance
        simulation = new Gravity3DSimulation('canvas-container');
        
        // Setup tab switching
        setupTabs();
        
        // Setup control panel handlers
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
            console.log('Switching to tab:', targetTab);
            
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            
            tabContents.forEach(content => content.classList.remove('active'));
            const targetPanel = document.getElementById(`${targetTab}-panel`);
            if (targetPanel) {
                targetPanel.classList.add('active');
                if (targetTab === 'inspect') refreshInspectPanel();
            } else {
                console.error('Panel not found:', `${targetTab}-panel`);
            }
        });
    });
    
    setInterval(() => {
        const inspectPanel = document.getElementById('inspect-panel');
        if (inspectPanel && inspectPanel.classList.contains('active')) {
            refreshInspectPanel();
        }
    }, 500);
    
    console.log('Tabs setup complete, found', tabButtons.length, 'tab buttons');
}

function setupControls() {
    // Add Planet button
    document.getElementById('add-planet-button').addEventListener('click', () => {
        const planetData = getPlanetData();
        if (planetData) {
            simulation.addPlanet(planetData);
        } else {
            alert('Please enter valid numbers!');
        }
    });
    
    // Clear button
    document.getElementById('clear-button').addEventListener('click', () => {
        if (confirm('Are you sure you want to clear the simulation?')) {
            simulation.clearSimulation();
        }
    });
    
    // G slider (% of real value)
    const gravitySlider = document.getElementById('gravity-slider');
    const gravityValue = document.getElementById('gravity-value');
    gravitySlider.addEventListener('input', (e) => {
        const pct = parseInt(e.target.value);
        gravityValue.textContent = pct;
        simulation.setG(pct);
    });
    // c slider (% of real value)
    const speedOfLightSlider = document.getElementById('speed-of-light-slider');
    const speedOfLightValue = document.getElementById('speed-of-light-value');
    speedOfLightSlider.addEventListener('input', (e) => {
        const pct = parseInt(e.target.value);
        speedOfLightValue.textContent = pct;
        simulation.setC(pct);
    });
    // σ slider (% of real value)
    const sigmaSlider = document.getElementById('sigma-slider');
    const sigmaValue = document.getElementById('sigma-value');
    sigmaSlider.addEventListener('input', (e) => {
        const pct = parseInt(e.target.value);
        sigmaValue.textContent = pct;
        simulation.setSigma(pct);
    });
    // Time slider: simulation days per real second (timeFactor = daysPerSec * 86400)
    const timeFactorSlider = document.getElementById('timefactor-slider');
    const timeFactorValue = document.getElementById('timefactor-value');
    timeFactorSlider.addEventListener('input', (e) => {
        const daysPerSec = parseInt(e.target.value);
        timeFactorValue.textContent = daysPerSec;
        simulation.setTimeFactor(daysPerSec * 86400);
    });
    
    // Bounce checkbox
    document.getElementById('bounce-checkbox').addEventListener('change', (e) => {
        simulation.setBounce(e.target.checked);
    });
    
    // RK4 checkbox
    document.getElementById('rk4-checkbox').addEventListener('change', (e) => {
        simulation.setRK4(e.target.checked);
    });
    
    // Update pause indicator
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

function refreshInspectPanel() {
    if (!simulation) return;
    const detailEl = document.getElementById('inspect-detail');
    const listEl = document.getElementById('inspect-list');
    const listWrap = document.getElementById('inspect-list-wrap');
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
        const img = document.getElementById('inspect-image');
        const path = selected.getTexturepath();
        const wrap = detailEl.querySelector('.inspect-image-wrap');
        if (path) {
            img.src = path;
            img.alt = selected.getName();
            img.style.display = 'block';
            wrap.style.background = '#2a2a2a';
        } else {
            img.style.display = 'none';
            const c = selected.getColor();
            wrap.style.background = c ? `rgb(${Math.round(255*c.r)},${Math.round(255*c.g)},${Math.round(255*c.b)})` : '#2a2a2a';
        }
        const pos = selected.getPosition();
        const vel = selected.getVelocity();
        const lumSol = selected.getLuminosity();
        const fieldsEl = document.getElementById('inspect-fields');
        fieldsEl.innerHTML = [
            ['Name', selected.getName()],
            ['Type', selected.state.getType()],
            ['Mass (kg)', formatNum(selected.getMass())],
            ['Radius (m)', formatNum(selected.getRadius())],
            ['Temperature (K)', formatNum(selected.getTemperature())],
            ['Luminosity (L☉)', lumSol === null ? '—' : formatNum(lumSol)],
            ['Angular vel.', formatNum(selected.getAngularVelocity())],
            ['Position (m)', `(${formatNum(pos.get(0))}, ${formatNum(pos.get(1))}, ${pos.dimensions() > 2 ? formatNum(pos.get(2)) : '0'})`],
            ['Velocity (m/s)', `(${formatNum(vel.get(0))}, ${formatNum(vel.get(1))}, ${vel.dimensions() > 2 ? formatNum(vel.get(2)) : '0'})`]
        ].map(([k, v]) => `<div class="row"><span>${k}</span><span>${v}</span></div>`).join('');
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
