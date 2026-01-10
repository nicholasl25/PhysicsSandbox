/**
 * 3D Gravity Simulation using Three.js.
 * Renders planets in 3D space and handles user interactions.
 */

// Global variables
let scene, camera, renderer, controls;
let planetMeshes = new Map(); // Map planet IDs to Three.js meshes
let apiClient;
let isPaused = false;

// Initialize the simulation
async function init() {
    // Create API client
    apiClient = new APIClient();
    
    // Create Three.js scene
    scene = new THREE.Scene();
    scene.background = new THREE.Color(0x000000);
    
    // Create camera
    camera = new THREE.PerspectiveCamera(
        75,
        window.innerWidth / window.innerHeight,
        0.1,
        10000
    );
    camera.position.set(0, 50, 100);
    camera.lookAt(0, 0, 0);
    
    // Create renderer
    const canvas = document.getElementById('canvas');
    renderer = new THREE.WebGLRenderer({ canvas: canvas, antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(window.devicePixelRatio);
    
    // Add orbit controls for camera
    // Try to use OrbitControls from CDN, fallback to basic implementation if not available
    if (typeof THREE.OrbitControls !== 'undefined') {
        controls = new THREE.OrbitControls(camera, renderer.domElement);
        controls.enableDamping = true;
        controls.dampingFactor = 0.05;
        controls.minDistance = 10;
        controls.maxDistance = 500;
    } else {
        // Basic fallback - manual camera controls
        console.warn('OrbitControls not found, using basic mouse controls');
        setupBasicControls();
    }
    
    // Add lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
    scene.add(ambientLight);
    
    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
    directionalLight.position.set(50, 50, 50);
    scene.add(directionalLight);
    
    // Add grid helper
    const gridHelper = new THREE.GridHelper(200, 20, 0x444444, 0x222222);
    scene.add(gridHelper);
    
    // Add axes helper
    const axesHelper = new THREE.AxesHelper(50);
    scene.add(axesHelper);
    
    // Handle window resize
    window.addEventListener('resize', onWindowResize);
    
    // Setup UI event listeners
    setupUI();
    
    // Create simulation and start polling
    try {
        showStatus('Creating simulation...', 'info');
        const simId = await apiClient.createSimulation3D();
        console.log('Simulation created:', simId);
        showStatus('Simulation ready!', 'success');
        
        // Start polling for state updates
        apiClient.startPolling(updatePlanets, 16); // ~60 FPS
        
        // Start animation loop
        animate();
    } catch (error) {
        showStatus('Error: ' + error.message, 'error');
        console.error('Failed to initialize simulation:', error);
    }
}

/**
 * Updates planet meshes based on simulation state.
 */
function updatePlanets(state) {
    // Update planet meshes
    const planetIds = new Set();
    
    if (state.planets) {
        state.planets.forEach(planet => {
            planetIds.add(getPlanetId(planet));
            
            let mesh = planetMeshes.get(getPlanetId(planet));
            
            // Create mesh if it doesn't exist
            if (!mesh) {
                const geometry = new THREE.SphereGeometry(planet.radius, 32, 32);
                const material = new THREE.MeshStandardMaterial({
                    color: new THREE.Color(
                        planet.color.r / 255,
                        planet.color.g / 255,
                        planet.color.b / 255
                    ),
                    metalness: 0.3,
                    roughness: 0.7
                });
                mesh = new THREE.Mesh(geometry, material);
                scene.add(mesh);
                planetMeshes.set(getPlanetId(planet), mesh);
            }
            
            // Update position
            mesh.position.set(
                planet.position.x,
                planet.position.y,
                planet.position.z
            );
            
            // Update rotation
            if (planet.rotationAngle !== undefined) {
                mesh.rotation.y = planet.rotationAngle;
            }
            
            // Update size if radius changed (rare, but possible with merges)
            if (Math.abs(mesh.geometry.parameters.radius - planet.radius) > 0.1) {
                mesh.geometry.dispose();
                mesh.geometry = new THREE.SphereGeometry(planet.radius, 32, 32);
            }
        });
    }
    
    // Remove meshes for planets that no longer exist
    for (const [id, mesh] of planetMeshes.entries()) {
        if (!planetIds.has(id)) {
            scene.remove(mesh);
            mesh.geometry.dispose();
            mesh.material.dispose();
            planetMeshes.delete(id);
        }
    }
}

/**
 * Gets a unique ID for a planet (for tracking).
 * Uses a combination of properties since Planet doesn't have a UUID yet.
 * For better tracking, we could add a UUID field to Planet in the future.
 */
function getPlanetId(planet) {
    // Use position, mass, and radius as a composite key
    // This works well enough for now since planets maintain these properties
    // In a production system, each planet should have a unique ID from the backend
    return `${planet.position.x.toFixed(3)}_${planet.position.y.toFixed(3)}_${planet.position.z.toFixed(3)}_${planet.mass}_${planet.radius}`;
}

/**
 * Animation loop.
 */
function animate() {
    requestAnimationFrame(animate);
    
    // Update controls (if OrbitControls is available)
    if (controls && controls.update) {
        controls.update();
    }
    
    // Render scene
    renderer.render(scene, camera);
}

/**
 * Basic mouse controls fallback if OrbitControls is not available.
 */
function setupBasicControls() {
    let isDragging = false;
    let previousMousePosition = { x: 0, y: 0 };
    let rotationX = 0;
    let rotationY = 0;
    let distance = 100;
    
    // Initialize camera position
    updateCameraPosition();
    
    function updateCameraPosition() {
        const x = distance * Math.sin(rotationY) * Math.cos(rotationX);
        const y = distance * Math.sin(rotationX);
        const z = distance * Math.cos(rotationY) * Math.cos(rotationX);
        camera.position.set(x, y, z);
        camera.lookAt(0, 0, 0);
    }
    
    renderer.domElement.addEventListener('mousedown', (e) => {
        isDragging = true;
        previousMousePosition = { x: e.clientX, y: e.clientY };
    });
    
    renderer.domElement.addEventListener('mousemove', (e) => {
        if (!isDragging) return;
        
        const deltaX = e.clientX - previousMousePosition.x;
        const deltaY = e.clientY - previousMousePosition.y;
        
        rotationY -= deltaX * 0.01;
        rotationX += deltaY * 0.01;
        rotationX = Math.max(-Math.PI / 2 + 0.1, Math.min(Math.PI / 2 - 0.1, rotationX));
        
        updateCameraPosition();
        previousMousePosition = { x: e.clientX, y: e.clientY };
    });
    
    renderer.domElement.addEventListener('mouseup', () => {
        isDragging = false;
    });
    
    renderer.domElement.addEventListener('wheel', (e) => {
        e.preventDefault();
        distance += e.deltaY * 0.1;
        distance = Math.max(10, Math.min(500, distance));
        updateCameraPosition();
    });
}

/**
 * Handles window resize.
 */
function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
}

/**
 * Sets up UI event listeners.
 */
function setupUI() {
    // Pause/Resume button
    const pauseBtn = document.getElementById('pause-btn');
    pauseBtn.addEventListener('click', async () => {
        isPaused = !isPaused;
        try {
            await apiClient.updateSettings({ paused: isPaused });
            pauseBtn.textContent = isPaused ? 'Resume' : 'Pause';
            showStatus(isPaused ? 'Simulation paused' : 'Simulation resumed', 'info');
        } catch (error) {
            showStatus('Error: ' + error.message, 'error');
        }
    });
    
    // Add Planet button
    const addPlanetBtn = document.getElementById('add-planet-btn');
    addPlanetBtn.addEventListener('click', async () => {
        try {
            const planetData = {
                mass: parseFloat(document.getElementById('mass').value),
                radius: parseFloat(document.getElementById('radius').value),
                position: {
                    x: parseFloat(document.getElementById('pos-x').value),
                    y: parseFloat(document.getElementById('pos-y').value),
                    z: parseFloat(document.getElementById('pos-z').value)
                },
                velocity: {
                    x: parseFloat(document.getElementById('vel-x').value),
                    y: parseFloat(document.getElementById('vel-y').value),
                    z: parseFloat(document.getElementById('vel-z').value)
                },
                color: hexToRgb(document.getElementById('color').value),
                name: document.getElementById('name').value || null,
                fixedLocation: document.getElementById('fixed-location').checked,
                angularVelocity: 0.0,
                temperature: 0.0
            };
            
            await apiClient.addPlanet(planetData);
            showStatus('Planet added!', 'success');
            
            // Clear form (except color)
            document.getElementById('mass').value = 1000;
            document.getElementById('radius').value = 10;
            document.getElementById('pos-x').value = 0;
            document.getElementById('pos-y').value = 0;
            document.getElementById('pos-z').value = 0;
            document.getElementById('vel-x').value = 0;
            document.getElementById('vel-y').value = 0;
            document.getElementById('vel-z').value = 0;
            document.getElementById('name').value = '';
            document.getElementById('fixed-location').checked = false;
        } catch (error) {
            showStatus('Error: ' + error.message, 'error');
        }
    });
    
    // Gravity slider
    const gravitySlider = document.getElementById('gravity-slider');
    const gravityValue = document.getElementById('gravity-value');
    gravitySlider.addEventListener('input', async (e) => {
        const value = e.target.value;
        gravityValue.textContent = value;
        try {
            await apiClient.updateSettings({ gravitationalConstant: parseFloat(value) });
        } catch (error) {
            console.error('Error updating gravity:', error);
        }
    });
    
    // Time factor slider
    const timeSlider = document.getElementById('time-slider');
    const timeValue = document.getElementById('time-value');
    timeSlider.addEventListener('input', async (e) => {
        const value = e.target.value;
        timeValue.textContent = parseFloat(value).toFixed(1);
        try {
            await apiClient.updateSettings({ timeFactor: parseFloat(value) });
        } catch (error) {
            console.error('Error updating time factor:', error);
        }
    });
    
    // Bounce checkbox
    const bounceCheckbox = document.getElementById('bounce-checkbox');
    bounceCheckbox.addEventListener('change', async (e) => {
        try {
            await apiClient.updateSettings({ bounce: e.target.checked });
        } catch (error) {
            console.error('Error updating bounce:', error);
        }
    });
    
    // Clear button
    const clearBtn = document.getElementById('clear-btn');
    clearBtn.addEventListener('click', async () => {
        if (confirm('Are you sure you want to clear all planets?')) {
            try {
                const response = await fetch(`${apiClient.baseURL}/api/simulations/${apiClient.simulationId}/planets`, {
                    method: 'DELETE'
                });
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                
                // Clear all meshes
                for (const [id, mesh] of planetMeshes.entries()) {
                    scene.remove(mesh);
                    mesh.geometry.dispose();
                    mesh.material.dispose();
                }
                planetMeshes.clear();
                
                showStatus('All planets cleared!', 'success');
            } catch (error) {
                showStatus('Error: ' + error.message, 'error');
            }
        }
    });
}

/**
 * Shows status message.
 */
function showStatus(message, type = 'info') {
    const statusEl = document.getElementById('status');
    statusEl.textContent = message;
    statusEl.className = 'status ' + type;
    
    // Auto-hide after 3 seconds
    setTimeout(() => {
        statusEl.textContent = '';
        statusEl.className = 'status';
    }, 3000);
}

/**
 * Converts hex color to RGB object.
 */
function hexToRgb(hex) {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : { r: 255, g: 0, b: 0 };
}

// Initialize when page loads
window.addEventListener('load', init);
