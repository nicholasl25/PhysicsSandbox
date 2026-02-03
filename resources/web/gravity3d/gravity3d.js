/**
 * Gravity3D Web Simulation using Three.js
 * Port of the Java Gravity3DSimulation to JavaScript/WebGL
 */

// Import dependencies (assuming Three.js is loaded via CDN)
// Vector and Planet classes should be loaded before this

class Gravity3DSimulation {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) {
            throw new Error(`Container with id "${containerId}" not found`);
        }
        
        // Simulation state
        this.planets = [];
        this.selectedPlanet = null;
        this.isPaused = false;
        
        // Simulation parameters
        this.gravitationalConstant = 6000.0;
        this.bounce = false;
        this.useRK4 = false;
        this.coefficientOfRestitution = 1.0;
        this.timeFactor = 1.0;
        this.deltaTime = 1.0 / 60.0; // 60 FPS
        
        // Three.js setup
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.controls = null;
        this.starsBackground = null;
        
        // Camera state
        this.cameraDistance = 200.0;
        this.cameraYaw = 0.0;
        this.cameraPitch = 0.0;
        
        // Mouse controls
        this.isDragging = false;
        this.mouseX = 0;
        this.mouseY = 0;
        this.lastMouseX = 0;
        this.lastMouseY = 0;
        
        // Texture loader
        this.textureLoader = new THREE.TextureLoader();
        this.textureCache = {};
        
        // Animation frame ID
        this.animationFrameId = null;
        
        this.initialize();
    }
    
    initialize() {
        // Check if Three.js is loaded
        if (typeof THREE === 'undefined') {
            console.error('Three.js is not loaded!');
            alert('Error: Three.js library failed to load. Please check your internet connection.');
            return;
        }
        
        // Wait for container to have dimensions
        if (this.container.clientWidth === 0 || this.container.clientHeight === 0) {
            console.warn('Container has zero dimensions, waiting...');
            setTimeout(() => this.initialize(), 100);
            return;
        }
        
        // Create scene
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0x000000);
        
        // Add lighting
        const ambientLight = new THREE.AmbientLight(0x404040, 0.5);
        this.scene.add(ambientLight);
        
        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
        directionalLight.position.set(100, 100, 100);
        this.scene.add(directionalLight);
        
        // Create camera
        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        const aspect = width / height;
        this.camera = new THREE.PerspectiveCamera(60, aspect, 0.1, 10000);
        this.camera.position.set(0, 0, this.cameraDistance);
        
        // Create renderer
        this.renderer = new THREE.WebGLRenderer({ antialias: true });
        this.renderer.setSize(width, height);
        this.renderer.setPixelRatio(window.devicePixelRatio);
        this.container.appendChild(this.renderer.domElement);
        
        console.log('Three.js initialized, container size:', width, 'x', height);
        
        // Load stars background texture
        this.loadStarsBackground();
        
        // Setup controls
        this.setupControls();
        
        // Setup initial planets
        this.setupPlanets();
        
        // Handle window resize
        window.addEventListener('resize', () => this.onWindowResize());
        
        // Start animation loop
        this.animate();
    }
    
    loadStarsBackground() {
        this.textureLoader.load('/textures/Stars.png', (texture) => {
            // Create a large sphere with stars texture for background
            const geometry = new THREE.SphereGeometry(5000, 32, 32);
            const material = new THREE.MeshBasicMaterial({
                map: texture,
                side: THREE.BackSide
            });
            this.starsBackground = new THREE.Mesh(geometry, material);
            this.scene.add(this.starsBackground);
        }, undefined, (error) => {
            console.warn('Could not load stars background:', error);
        });
    }
    
    setupControls() {
        // Mouse controls for camera rotation
        this.renderer.domElement.addEventListener('mousedown', (e) => {
            if (e.button === 0) { // Left mouse button
                this.isDragging = true;
                this.lastMouseX = e.clientX;
                this.lastMouseY = e.clientY;
            }
        });
        
        this.renderer.domElement.addEventListener('mousemove', (e) => {
            if (this.isDragging) {
                const deltaX = e.clientX - this.lastMouseX;
                const deltaY = e.clientY - this.lastMouseY;
                
                this.cameraYaw += deltaX * 0.01;
                this.cameraPitch += deltaY * 0.01;
                
                // Clamp pitch
                const maxPitch = Math.PI / 2 - 0.1;
                this.cameraPitch = Math.max(-maxPitch, Math.min(maxPitch, this.cameraPitch));
                
                this.lastMouseX = e.clientX;
                this.lastMouseY = e.clientY;
                
                this.updateCameraPosition();
            }
        });
        
        this.renderer.domElement.addEventListener('mouseup', (e) => {
            if (e.button === 0) {
                this.isDragging = false;
            }
        });
        
        this.renderer.domElement.addEventListener('mouseleave', () => {
            this.isDragging = false;
        });
        
        // Click to select planet
        this.renderer.domElement.addEventListener('click', (e) => {
            if (!this.isDragging) {
                this.handleClick(e);
            }
        });
        
        // Spacebar to pause/resume
        document.addEventListener('keydown', (e) => {
            if (e.code === 'Space' && e.target === document.body) {
                e.preventDefault();
                this.isPaused = !this.isPaused;
            }
        });
        
        // Wheel for zoom
        this.renderer.domElement.addEventListener('wheel', (e) => {
            e.preventDefault();
            const zoomFactor = Math.pow(1.1, -e.deltaY / 100);
            this.cameraDistance *= zoomFactor;
            const minDistance = this.selectedPlanet ? this.selectedPlanet.getRadius() : 0;
            this.cameraDistance = Math.max(minDistance + 1, this.cameraDistance);
            console.log('minDistance:', minDistance, 'cameraDistance:', this.cameraDistance, 'zoomFactor:', zoomFactor);
            this.updateCameraPosition();
        });
    }
    
    updateCameraPosition() {
        if (!this.selectedPlanet || this.planets.length === 0) {
            // Default camera position
            const cosPitch = Math.cos(this.cameraPitch);
            const sinPitch = Math.sin(this.cameraPitch);
            const cosYaw = Math.cos(this.cameraYaw);
            const sinYaw = Math.sin(this.cameraYaw);
            
            const offsetX = this.cameraDistance * cosPitch * sinYaw;
            const offsetY = this.cameraDistance * sinPitch;
            const offsetZ = -this.cameraDistance * cosPitch * cosYaw;
            
            this.camera.position.set(offsetX, offsetY, offsetZ);
            this.camera.lookAt(0, 0, 0);
            return;
        }
        
        // Camera follows selected planet (rest frame - selected planet is at origin)
        const cosPitch = Math.cos(this.cameraPitch);
        const sinPitch = Math.sin(this.cameraPitch);
        const cosYaw = Math.cos(this.cameraYaw);
        const sinYaw = Math.sin(this.cameraYaw);
        
        const offsetX = this.cameraDistance * cosPitch * sinYaw;
        const offsetY = this.cameraDistance * sinPitch;
        const offsetZ = -this.cameraDistance * cosPitch * cosYaw;
        
        // Selected planet is always at origin in rest frame
        this.camera.position.set(offsetX, offsetY, offsetZ);
        this.camera.lookAt(0, 0, 0);
    }
    
    handleClick(event) {
        const rect = this.renderer.domElement.getBoundingClientRect();
        const mouse = new THREE.Vector2();
        mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        const raycaster = new THREE.Raycaster();
        raycaster.setFromCamera(mouse, this.camera);
        
        // Get origin position for rest frame
        let originX = 0, originY = 0, originZ = 0;
        if (this.selectedPlanet) {
            const originPos = this.selectedPlanet.getPosition();
            originX = originPos.get(0);
            originY = originPos.get(1);
            originZ = originPos.dimensions() > 2 ? originPos.get(2) : 0;
        }
        
        // Check intersections with all planets
        // Meshes are already positioned in rest frame, so we can use them directly
        let clickedPlanet = null;
        let minDistance = Infinity;
        
        for (const planet of this.planets) {
            if (!planet.mesh) continue;
            
            const intersects = raycaster.intersectObject(planet.mesh);
            
            if (intersects.length > 0) {
                const distance = intersects[0].distance;
                if (distance < minDistance) {
                    minDistance = distance;
                    clickedPlanet = planet;
                }
            }
        }
        
        // Update selection
        if (this.selectedPlanet) {
            this.selectedPlanet.clicked();
            this.updatePlanetHighlight(this.selectedPlanet, false);
        }
        
        this.selectedPlanet = clickedPlanet;
        if (this.selectedPlanet) {
            this.selectedPlanet.clicked();
            this.updatePlanetHighlight(this.selectedPlanet, true);
            const minDistance = this.selectedPlanet.getRadius();
            if (this.cameraDistance < minDistance) {
                this.cameraDistance = minDistance;
            }
        }
        
        this.updateCameraPosition();
    }
    
    updatePlanetHighlight(planet, selected) {
        if (!planet.mesh) return;
        
        // Add/remove highlight (emissive glow)
        if (selected) {
            // Add highlight effect
            planet.mesh.material.emissive = new THREE.Color(0x333333);
        } else {
            planet.mesh.material.emissive = new THREE.Color(0x000000);
        }
    }
    
    loadTexture(texturePath, callback) {
        if (this.textureCache[texturePath]) {
            callback(this.textureCache[texturePath]);
            return;
        }
        
        this.textureLoader.load(texturePath, (texture) => {
            texture.wrapS = THREE.RepeatWrapping;
            texture.wrapT = THREE.RepeatWrapping;
            this.textureCache[texturePath] = texture;
            callback(texture);
        }, undefined, (error) => {
            console.warn('Could not load texture:', texturePath, error);
            callback(null);
        });
    }
    
    createPlanetMesh(planet) {
        const geometry = new THREE.SphereGeometry(planet.getRadius(), 32, 16);
        
        // Default material (will be updated if texture loads)
        const defaultColor = planet.state.getColor() || new THREE.Color(0.3, 0.5, 1.0);
        let material = new THREE.MeshStandardMaterial({
            color: defaultColor,
            emissive: new THREE.Color(0x000000)
        });
        
        const mesh = new THREE.Mesh(geometry, material);
        planet.mesh = mesh;
        this.scene.add(mesh);
        
        // Load texture if provided
        if (planet.state.getTexturepath()) {
            
            const tintColor = planet.state.getColor();

            this.loadTexture(planet.state.getTexturepath(), (texture) => {
                if (texture) {
                    material = new THREE.MeshStandardMaterial({
                        map: texture,
                        color: tintColor,
                        emissive: tintColor.clone().multiplyScalar(0.5)
                    });
                    mesh.material = material;
                }
            });
        }
        
        return mesh;
    }
    
    addPlanet(planetData) {
        const angularVelocity = planetData.period > 0 
            ? (2.0 * Math.PI) / planetData.period 
            : 0.0;
        
        const pos = new Vector([planetData.x, planetData.y, planetData.z]);
        const vel = new Vector([planetData.vx, planetData.vy, planetData.vz]);
        
        const state = new State(
            planetData.mass,
            planetData.radius,
            planetData.temperature,
        )

        const planet = new Planet(
            pos,
            vel,
            angularVelocity,
            planetData.name || `Planet #${this.planets.length}`,
            state,
        );
        
        this.planets.push(planet);
        this.createPlanetMesh(planet);
        
        // Position the new planet
        this.updatePlanetPositions();
        
        // Always select the newly added planet
        if (this.selectedPlanet) {
            this.selectedPlanet.clicked();
            this.updatePlanetHighlight(this.selectedPlanet, false);
        }
        this.selectedPlanet = planet;
        planet.clicked();
        this.updatePlanetHighlight(planet, true);
        const minDistance = planet.getRadius();
        if (this.cameraDistance < minDistance) {
            this.cameraDistance = minDistance;
        }
        
        this.updateCameraPosition();
    }
    
    clearSimulation() {
        // Remove all planet meshes
        for (const planet of this.planets) {
            if (planet.mesh) {
                this.scene.remove(planet.mesh);
                planet.mesh.geometry.dispose();
                planet.mesh.material.dispose();
            }
        }
        
        if (this.selectedPlanet) {
            this.selectedPlanet.clicked();
        }
        this.selectedPlanet = null;
        this.planets = [];
        this.updateCameraPosition();
    }
    
    setupPlanets() {
        // Create a sun at the origin
        const sunState = new State(1000.0, 20.0, 5778.0);
        const earthState = new State(50, 10, 288);


        const sun = new Planet(
            new Vector([0.0, 0.0, 0.0]),
            new Vector([0.0, 0.0, 0.0]),
            0.02, 'Sun', sunState
        );
        
        // Create a planet orbiting the sun
        const earth = new Planet(
            new Vector([60.0, 20.0, 0.0]),
            new Vector([0.0, -8.0, 0.0]),
            0.06, 'Earth', earthState
        );
        
        this.planets.push(sun);
        this.planets.push(earth);
        
        // Create meshes
        for (const planet of this.planets) {
            this.createPlanetMesh(planet);
        }
        
        // Position meshes initially (in rest frame, selected planet is at origin)
        this.updatePlanetPositions();
        
        // Select first planet
        this.selectedPlanet = sun;
        sun.clicked();
        this.updatePlanetHighlight(sun, true);
        const minDistance = sun.getRadius();
        if (this.cameraDistance < minDistance) {
            this.cameraDistance = minDistance;
        }
        this.updateCameraPosition();
        
        console.log('Planets created:', this.planets.length);
    }
    
    updatePlanetPositions() {
        // Get origin position for rest frame
        let originX = 0, originY = 0, originZ = 0;
        if (this.selectedPlanet) {
            const originPos = this.selectedPlanet.getPosition();
            originX = originPos.get(0);
            originY = originPos.get(1);
            originZ = originPos.dimensions() > 2 ? originPos.get(2) : 0;
        }
        
        // Update planet meshes in rest frame
        for (const planet of this.planets) {
            if (!planet.mesh) continue;
            
            const pos = planet.getPosition();
            const x = pos.get(0) - originX;
            const y = pos.get(1) - originY;
            const z = (pos.dimensions() > 2 ? pos.get(2) : 0) - originZ;
            
            planet.mesh.position.set(x, y, z);
            planet.mesh.rotation.y = planet.getRotationAngle();
        }
    }
    
    update(deltaTime) {
        if (this.isPaused) {
            return;
        }
        
        // Ensure a planet is selected
        if (!this.selectedPlanet && this.planets.length > 0) {
            this.selectedPlanet = this.planets[0];
            this.selectedPlanet.clicked();
            this.updatePlanetHighlight(this.selectedPlanet, true);
        }
        
        // Get origin position for rest frame
        let originX = 0, originY = 0, originZ = 0;
        if (this.selectedPlanet) {
            const originPos = this.selectedPlanet.getPosition();
            originX = originPos.get(0);
            originY = originPos.get(1);
            originZ = originPos.dimensions() > 2 ? originPos.get(2) : 0;
        }
        
        const toAdd = [];
        const toRemove = [];
        
        // Calculate forces and update velocities
        for (const planet of this.planets) {
            if (toRemove.includes(planet)) continue;
            
            let totalForce = new Vector([0.0, 0.0, 0.0]);
            
            for (const other of this.planets) {
                if (planet === other) continue;
                if (toRemove.includes(other)) continue;
                if (toRemove.includes(planet)) break;
                
                // Check for collisions
                if (planet.collidesWith(other)) {
                    if (this.bounce) {
                        planet.bouncePlanet(this.coefficientOfRestitution, other);
                    } else {
                        // Handle merge
                        const merged = planet.merge(other);
                        toAdd.push(merged);
                        toRemove.push(planet);
                        toRemove.push(other);
                    }
                    break;
                }
                
                // Compute gravitational force
                const forceVec = planet.gravitationalForceFrom(other, this.gravitationalConstant);
                totalForce = totalForce.add(forceVec);
            }
            
            if (toRemove.includes(planet)) continue;
            
            // Newton's second law: F = ma → a = F/m
            const acceleration = totalForce.divide(planet.getMass());
            planet.updateVelocity(acceleration, deltaTime * this.timeFactor);
        }
        
        // Apply removals and additions
        for (const planet of toRemove) {
            const index = this.planets.indexOf(planet);
            if (index > -1) {
                if (planet.mesh) {
                    this.scene.remove(planet.mesh);
                    planet.mesh.geometry.dispose();
                    planet.mesh.material.dispose();
                }
                this.planets.splice(index, 1);
            }
        }
        
        for (const merged of toAdd) {
            this.planets.push(merged);
            this.createPlanetMesh(merged);
            // If selected planet was merged, select the merged planet
            if (this.selectedPlanet && (toRemove.includes(this.selectedPlanet))) {
                this.selectedPlanet = merged;
                merged.clicked();
                this.updatePlanetHighlight(merged, true);
            }
        }
        
        // If selected planet was removed (but not merged), clear selection
        if (this.selectedPlanet && !this.planets.includes(this.selectedPlanet)) {
            this.selectedPlanet = null;
        }
        
        // Update positions
        for (const planet of this.planets) {
            planet.updatePosition(deltaTime, this.timeFactor);
        }
        
        // Update camera
        this.updateCameraPosition();
        
        // Update planet meshes in rest frame
        this.updatePlanetPositions();
    }
    
    render() {
        if (!this.renderer || !this.scene || !this.camera) {
            return;
        }
        
        this.renderer.render(this.scene, this.camera);
        
        // Draw pause indicator
        if (this.isPaused) {
            // Could add a UI overlay here
        }
    }
    
    animate() {
        this.animationFrameId = requestAnimationFrame(() => this.animate());
        this.update(this.deltaTime);
        this.render();
    }
    
    onWindowResize() {
        const width = this.container.clientWidth;
        const height = this.container.clientHeight;
        
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(width, height);
    }
    
    // Public API for control panel
    setGravitationalConstant(value) {
        this.gravitationalConstant = value;
    }
    
    setTimeFactor(value) {
        this.timeFactor = value;
    }
    
    setBounce(enabled) {
        this.bounce = enabled;
    }
    
    setRK4(enabled) {
        this.useRK4 = enabled; // Not implemented yet
    }
    
    dispose() {
        if (this.animationFrameId) {
            cancelAnimationFrame(this.animationFrameId);
        }
        
        // Clean up all meshes
        this.clearSimulation();
        
        // Clean up renderer
        if (this.renderer) {
            this.renderer.dispose();
        }
    }
}
