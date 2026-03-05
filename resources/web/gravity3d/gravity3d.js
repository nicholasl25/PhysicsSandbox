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
        
        // Physics constants (shared by Planet and State)
        this.consts = {
            G: 6.67430e-11,
            c: 299792458, 
            σ: 5.67e-8

        };
        this.bounce = false;
        this.useRK4 = false;
        this.coefficientOfRestitution = 1.0;
        this.timeFactor = 86400.0; // 86400 seconds in a day
        this.deltaTime = 1/60.0;  // 60 fps
        
        // Three.js setup
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.controls = null;
        this.starsBackground = null;
        
        // Display scale: physics (m) → scene units so huge radii fit in view (1 m = 1e-6 scene)
        this.displayScale = 1e-6;
        // Camera state (stored in physics units; scaled when setting position)
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
        this.starHaloTexture = null;
        
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
        
        // Add lighting: dim ambient + stars emit their own light
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        this.scene.add(ambientLight);
        
        // Create camera
        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        const aspect = width / height;
        this.camera = new THREE.PerspectiveCamera(60, aspect, 0.001, 1e9);
        this.camera.position.set(0, 0, this.cameraDistance * this.displayScale);
        
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
            texture.mapping = THREE.EquirectangularReflectionMapping;
            this.scene.background = texture;
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
            const minDistance = this.selectedPlanet ? this.getMinCameraDistanceFor(this.selectedPlanet) : 0;
            this.cameraDistance = Math.max(minDistance, this.cameraDistance);
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
            
            const offsetX = this.cameraDistance * cosPitch * sinYaw * this.displayScale;
            const offsetY = this.cameraDistance * sinPitch * this.displayScale;
            const offsetZ = -this.cameraDistance * cosPitch * cosYaw * this.displayScale;
            
            this.camera.position.set(offsetX, offsetY, offsetZ);
            this.camera.lookAt(0, 0, 0);
            return;
        }
        
        // Camera follows selected planet (rest frame - selected planet is at origin)
        const cosPitch = Math.cos(this.cameraPitch);
        const sinPitch = Math.sin(this.cameraPitch);
        const cosYaw = Math.cos(this.cameraYaw);
        const sinYaw = Math.sin(this.cameraYaw);
        
        const offsetX = this.cameraDistance * cosPitch * sinYaw * this.displayScale;
        const offsetY = this.cameraDistance * sinPitch * this.displayScale;
        const offsetZ = -this.cameraDistance * cosPitch * cosYaw * this.displayScale;
        
        // Selected planet is always at origin in rest frame
        this.camera.position.set(offsetX, offsetY, offsetZ);
        this.camera.lookAt(0, 0, 0);
    }
    
    updateCameraFrustum() {
        if (!this.camera || this.planets.length === 0) return;
        const cam = this.camera.position;
        let minDist = Infinity;
        let maxDist = 0;
        for (const planet of this.planets) {
            if (!planet.mesh) continue;
            const rScene = Math.max(planet.getRadius() * this.displayScale, 0.01);
            const dx = planet.mesh.position.x - cam.x;
            const dy = planet.mesh.position.y - cam.y;
            const dz = planet.mesh.position.z - cam.z;
            const d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            minDist = Math.min(minDist, Math.max(0, d - rScene));
            maxDist = Math.max(maxDist, d + rScene);
        }
        if (minDist === Infinity) minDist = 1;
        let near = Math.max(0.001, minDist * 0.1);
        let far = Math.max(maxDist * 2, 1000);
        const maxRatio = 1e6;
        if (far / near > maxRatio) near = far / maxRatio;
        this.camera.near = near;
        this.camera.far = far;
        this.camera.updateProjectionMatrix();
    }
    
    /** Minimum camera distance (physics units) so the body is at least 4× its visual radius away (small bodies use min scene radius 0.01). */
    getMinCameraDistanceFor(planet) {
        const visualRadiusScene = Math.max(planet.getRadius() * this.displayScale, 0.01);
        return (4 * visualRadiusScene) / this.displayScale;
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
    
    /**
     * Converts a loaded texture to grayscale (luminance only).
     * Use with color: tintColor to get "tintColor version of texture" without multiplication.
     */
    createGrayscaleTexture(threeTexture) {
        const img = threeTexture.image;
        if (!img || !img.complete) return threeTexture;
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0);
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const data = imageData.data;
        for (let i = 0; i < data.length; i += 4) {
            const lum = 0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2];
            data[i] = data[i + 1] = data[i + 2] = lum;
        }
        ctx.putImageData(imageData, 0, 0);
        const grayTexture = new THREE.CanvasTexture(canvas);
        grayTexture.wrapS = threeTexture.wrapS;
        grayTexture.wrapT = threeTexture.wrapT;
        return grayTexture;
    }

    getStarHaloTexture() {
        if (this.starHaloTexture) return this.starHaloTexture;

        const size = 512;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');

        const cx = size / 2;
        const cy = size / 2;
        const r = size / 2;

        // Ring-like halo (transparent center, bright border)
        const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, r);
        grad.addColorStop(0.0, 'rgba(255,255,255,0.80)');
        grad.addColorStop(0.1, 'rgba(255,255,255,0.70)');
        grad.addColorStop(0.2, 'rgba(255,255,255,0.55)');
        grad.addColorStop(0.3, 'rgba(255,255,255,0.40)');
        grad.addColorStop(0.4, 'rgba(255,255,255,0.25)');
        grad.addColorStop(0.5, 'rgba(255,255,255,0.10)');
        grad.addColorStop(0.6, 'rgba(255,255,255,0.05)');
        grad.addColorStop(0.7, 'rgba(255,255,255,0.02)');
        grad.addColorStop(0.8, 'rgba(255,255,255,0.01)');
        grad.addColorStop(0.9, 'rgba(255,255,255,0.00)');
        grad.addColorStop(1.0, 'rgba(255,255,255,0.00)');

        ctx.fillStyle = grad;
        ctx.fillRect(0, 0, size, size);

        const texture = new THREE.CanvasTexture(canvas);
        texture.minFilter = THREE.LinearMipMapLinearFilter;
        texture.magFilter = THREE.LinearFilter;
        this.starHaloTexture = texture;
        return texture;
    }

    disposePlanetMesh(planet) {
        if (!planet?.mesh) return;
        this.scene.remove(planet.mesh);
        planet.mesh.traverse((obj) => {
            if (obj.geometry) obj.geometry.dispose();
            if (obj.material) {
                if (Array.isArray(obj.material)) obj.material.forEach((m) => m.dispose());
                else obj.material.dispose();
            }
        });
        planet.mesh = null;
    }

    addStarLightAndHalo(mesh, planet, radiusScene, starColor) {
        const intensity = planet.getLuminosity(); // Scale by Sun-like luminosity
        const pointLight = new THREE.PointLight(starColor.getHex(), intensity, 0);
        pointLight.distance = 0; // Infinite range
        mesh.add(pointLight);
        planet.starLight = pointLight;

        const haloMat = new THREE.SpriteMaterial({
            map: this.getStarHaloTexture(),
            color: starColor,
            transparent: true,
            blending: THREE.AdditiveBlending,
            depthWrite: false,
        });
        haloMat.opacity = 0.9;
        const halo = new THREE.Sprite(haloMat);


        const LuminosityPerArea = planet.getLuminosityPerArea();
        // Illumination score ranges from 0 to 100 based on luminosity per area
        const illuminationScore = Math.min(Math.max(2*Math.log2(LuminosityPerArea) + 20, 0), 100);

        const haloDiameter = radiusScene * illuminationScore;
        halo.scale.set(haloDiameter, haloDiameter, 1);
        halo.renderOrder = 1;
        halo.frustumCulled = false;
        mesh.add(halo);
        planet.starHalo = halo;
    }
    
    createPlanetMesh(planet) {
        const radiusScene = Math.max(planet.getRadius() * this.displayScale, 0.01);
        const geometry = new THREE.SphereGeometry(radiusScene, 32, 16);
        
        const isStar = planet.state.type === PlanetTypes.STAR;
        const defaultColor = planet.state.getColor() || new THREE.Color(0.3, 0.5, 1.0);
        
        // Default material (will be updated if texture loads)
        let material = new THREE.MeshStandardMaterial({
            color: defaultColor,
            emissive: isStar ? defaultColor.clone() : new THREE.Color(0x000000),
            emissiveIntensity: isStar ? 0.8 : 0,
        });
        
        const mesh = new THREE.Mesh(geometry, material);
        planet.mesh = mesh;
        this.scene.add(mesh);
        
        // Stars emit light into the scene and have a colored halo/border
        if (isStar) {
            const starColor = planet.state.getColor() || defaultColor;
            this.addStarLightAndHalo(mesh, planet, radiusScene, starColor);
        }
        
        // Load texture if provided
        if (planet.state.getTexturepath()) {
            const tintColor = planet.state.getColor();
            const isStarType = planet.state.type === PlanetTypes.STAR;

            this.loadTexture(planet.state.getTexturepath(), (texture) => {
                if (texture) {
                    if (isStarType && tintColor) {
                        // Stars: emissive so they glow and emit light
                        const grayscaleMap = this.createGrayscaleTexture(texture);
                        material = new THREE.MeshStandardMaterial({
                            map: grayscaleMap,
                            color: tintColor,
                            emissive: tintColor.clone(),
                            emissiveIntensity: 0.5,
                        });
                    } else {
                        // Planets: use texture as-is
                        material = new THREE.MeshStandardMaterial({
                            map: texture,
                        });
                    }
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
            this.consts
        );

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
        }
        this.selectedPlanet = planet;
        planet.clicked();
        this.cameraDistance = this.getMinCameraDistanceFor(planet);
        this.updateCameraPosition();
    }
    
    clearSimulation() {
        // Remove all planet meshes
        for (const planet of this.planets) {
            this.disposePlanetMesh(planet);
        }
        
        if (this.selectedPlanet) {
            this.selectedPlanet.clicked();
        }
        this.selectedPlanet = null;
        this.planets = [];
        this.updateCameraPosition();
    }
    
    selectPlanet(planet) {
        if (!planet || !this.planets.includes(planet)) return;
        if (this.selectedPlanet === planet) return;
        if (this.selectedPlanet) this.selectedPlanet.clicked();
        this.selectedPlanet = planet;
        planet.clicked();
        this.cameraDistance = this.getMinCameraDistanceFor(planet);
        this.updateCameraPosition();
    }
    
    removePlanet(planet) {
        if (!planet || !this.planets.includes(planet)) return;
        const idx = this.planets.indexOf(planet);
        if (idx === -1) return;
        if (this.selectedPlanet === planet) {
            this.selectedPlanet.clicked();
            this.selectedPlanet = this.planets.length > 1 ? this.planets[idx === 0 ? 1 : 0] : null;
            if (this.selectedPlanet) {
                this.selectedPlanet.clicked();
                this.cameraDistance = this.getMinCameraDistanceFor(this.selectedPlanet);
            }
        }
        this.disposePlanetMesh(planet);
        this.planets.splice(idx, 1);
        this.updateCameraPosition();
    }
    
    setupPlanets() {
        // Real-world scale (kg, m). Display scale converts to scene units.
        const sunState = new State(1.9885e30, 6.957e8, 5778.0, this.consts);
        const earthState = new State(5.972e24, 6.371e6, 288, this.consts);
        const sun = new Planet(
            new Vector([0.0, 0.0, 0.0]),
            new Vector([0.0, 0.0, 0.0]),
            2.963e-6, 'Sun', sunState  // Sun rotation ~24.5 days → rad/s
        );
        const earthOrbitRadius = 1.496e11;
        const earth = new Planet(
            new Vector([earthOrbitRadius, 0.0, 0.0]),
            new Vector([0.0, 29780, 0.0]),
            7.272e-5, 'Earth', earthState  // Earth rotation 1 day → rad/s
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
        this.cameraDistance = this.getMinCameraDistanceFor(sun);
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
            const x = (pos.get(0) - originX) * this.displayScale;
            const y = (pos.get(1) - originY) * this.displayScale;
            const z = ((pos.dimensions() > 2 ? pos.get(2) : 0) - originZ) * this.displayScale;
            
            planet.mesh.position.set(x, y, z);
            planet.mesh.rotation.y = planet.getRotationAngle();
        }
    }
    
    /** For one planet: sum gravity from others, handle collision/merge, then F=ma and update velocity. */
    applyForcesToPlanet(planet, toRemove, toAdd, scaledDt) {
        let totalForce = new Vector([0.0, 0.0, 0.0]);
        for (const other of this.planets) {
            if (planet === other) continue;
            if (toRemove.includes(other)) continue;
            if (toRemove.includes(planet)) return;
            if (planet.collidesWith(other)) {
                if (this.bounce) {
                    planet.bouncePlanet(this.coefficientOfRestitution, other);
                } else {
                    const merged = planet.merge(other, this.consts);
                    toAdd.push(merged);
                    toRemove.push(planet);
                    toRemove.push(other);
                }
                return;
            }
            const forceVec = planet.gravitationalForceFrom(other, this.consts);
            totalForce = totalForce.add(forceVec);
        }
        const acceleration = totalForce.divide(planet.getMass());
        planet.updateVelocity(acceleration, scaledDt);
    }

    applyRadiationToPlanet(planet, toRemove, toAdd, scaledDt) {
        for (const other of this.planets) {
            if (planet === other) continue;
            if (toRemove.includes(other)) continue;
            if (toRemove.includes(planet)) return;
            if (other.state.getType() !== PlanetTypes.STAR) continue;
            const distance = planet.distanceTo(other);
            if (distance > other.state.getRadius()) {
                planet.applyRadiationFrom(other, scaledDt);
            }
        }
    }

    radiateHeatAway(planet, toRemove, scaledDt) {
        if (toRemove.includes(planet)) return;
        if (planet.state.getType() === PlanetTypes.STAR) return;
        planet.applyRadiationAway(scaledDt);
    }


    update(deltaTime) {
        if (this.isPaused) {
            return;
        }
        
        // Ensure a planet is selected
        if (!this.selectedPlanet && this.planets.length > 0) {
            this.selectedPlanet = this.planets[0];
            this.selectedPlanet.clicked();
            this.cameraDistance = this.getMinCameraDistanceFor(this.selectedPlanet);
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
        const scaledDt = deltaTime * this.timeFactor;

        for (const planet of this.planets) {
            if (toRemove.includes(planet)) continue;
            this.applyForcesToPlanet(planet, toRemove, toAdd, scaledDt);
            this.applyRadiationToPlanet(planet, toRemove, toAdd, scaledDt);
            this.radiateHeatAway(planet, toRemove, scaledDt);
        }
        
        // Apply removals and additions
        for (const planet of toRemove) {
            const index = this.planets.indexOf(planet);
            if (index > -1) {
                this.disposePlanetMesh(planet);
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
                this.cameraDistance = this.getMinCameraDistanceFor(merged);
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
        this.updateCameraFrustum();
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
    setG(pct) {
        this.consts.G = (pct / 100) * 6.67430e-11;
    }
    setC(pct) {
        this.consts.c = (pct / 100) * 299792458;
    }
    setSigma(pct) {
        this.consts.σ = (pct / 100) * 5.67e-8;
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
