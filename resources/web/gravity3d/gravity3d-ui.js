/**
 * UI Controller for Gravity3D Web Simulation
 */

let simulation = null;

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
            
            // Update button states
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            
            // Update content visibility
            tabContents.forEach(content => content.classList.remove('active'));
            const targetPanel = document.getElementById(`${targetTab}-panel`);
            if (targetPanel) {
                targetPanel.classList.add('active');
            } else {
                console.error('Panel not found:', `${targetTab}-panel`);
            }
        });
    });
    
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
    
    // Gravity slider
    const gravitySlider = document.getElementById('gravity-slider');
    const gravityValue = document.getElementById('gravity-value');
    gravitySlider.addEventListener('input', (e) => {
        const value = parseInt(e.target.value);
        gravityValue.textContent = value;
        simulation.setGravitationalConstant(value);
    });
    
    // Time factor slider
    const timeFactorSlider = document.getElementById('timefactor-slider');
    const timeFactorValue = document.getElementById('timefactor-value');
    timeFactorSlider.addEventListener('input', (e) => {
        const value = parseInt(e.target.value) / 100.0;
        timeFactorValue.textContent = value.toFixed(2);
        simulation.setTimeFactor(value);
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

function getPlanetData() {
    try {
        const mass = parseFloat(document.getElementById('mass-field').value);
        const radius = parseFloat(document.getElementById('radius-field').value);
        const x = parseFloat(document.getElementById('x-field').value);
        const y = parseFloat(document.getElementById('y-field').value);
        const z = parseFloat(document.getElementById('z-field').value);
        const vx = parseFloat(document.getElementById('vx-field').value);
        const vy = parseFloat(document.getElementById('vy-field').value);
        const vz = parseFloat(document.getElementById('vz-field').value);
        const period = parseFloat(document.getElementById('period-field').value);
        const temperature = parseFloat(document.getElementById('temperature-field').value);
        const texture = document.getElementById('texture-field').value;
        const name = document.getElementById('name-field').value.trim();
        
        // Validate
        if (isNaN(mass) || isNaN(radius) || isNaN(x) || isNaN(y) || isNaN(z) ||
            isNaN(vx) || isNaN(vy) || isNaN(vz) || isNaN(period) || isNaN(temperature)) {
            return null;
        }
        
        // Get texture path
        let texturePath = null;
        if (texture) {
            texturePath = `/textures/${texture}.jpg`;
        }
        
        
        return {
            mass,
            radius,
            x,
            y,
            z,
            vx,
            vy,
            vz,
            period,
            temperature,
            texturePath,
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
