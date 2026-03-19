/**
 * Gravity3D Web Simulation - Core
 * Class definition, state, init, planet CRUD, simulation loop, public API.
 * Depends: Three.js, physics/Vector.js, physics/State.js, physics/Planet.js
 */

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
        this.deltaTime = 1 / 60.0;  // 60 fps

        // Three.js setup
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.controls = null;
        this.starsBackground = null;

        // Display scale: physics (m) → scene units
        this.displayScale = 1e-6;
        this.cameraDistance = 200.0;
        this.cameraYaw = 0.0;
        this.cameraPitch = 0.0;
        this.cameraOffsetX = 0;
        this.cameraOffsetY = 0;
        this.cameraOffsetZ = 0;

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

        this.animationFrameId = null;
        this.orbitTrails = new Map();
        this.orbitTrailMaxLength = 600;
        // Per-planet THREE.Line objects used for orbit visualization (depth-tested so planets occlude them).
        // Map: Planet -> { line, geometry, material }
        this.orbitLines = new Map();

        this.initialize();
    }

    initialize() {
        if (typeof THREE === 'undefined') {
            console.error('Three.js is not loaded!');
            alert('Error: Three.js library failed to load. Please check your internet connection.');
            return;
        }
        if (this.container.clientWidth === 0 || this.container.clientHeight === 0) {
            console.warn('Container has zero dimensions, waiting...');
            setTimeout(() => this.initialize(), 100);
            return;
        }

        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0x000000);
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
        this.scene.add(ambientLight);

        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        const aspect = width / height;
        this.camera = new THREE.PerspectiveCamera(60, aspect, 0.001, 1e9);
        this.camera.position.set(0, 0, this.cameraDistance * this.displayScale);

        this.renderer = new THREE.WebGLRenderer({ antialias: true });
        this.renderer.setSize(width, height);
        this.renderer.setPixelRatio(window.devicePixelRatio);
        this.container.appendChild(this.renderer.domElement);

        this.markerCanvas = document.createElement('canvas');
        this.markerCanvas.style.cssText = 'position:absolute;left:0;top:0;width:100%;height:100%;pointer-events:none;';
        this.markerCanvas.width = width;
        this.markerCanvas.height = height;
        this.container.appendChild(this.markerCanvas);
        this.markerCtx = this.markerCanvas.getContext('2d');
        this.projectVector = new THREE.Vector3();
        this._viewDir = new THREE.Vector3();
        this._upPerp = new THREE.Vector3();
        this._radiusPoint = new THREE.Vector3();
        this._axisX = new THREE.Vector3(1, 0, 0);
        this._axisY = new THREE.Vector3(0, 1, 0);

        console.log('Three.js initialized, container size:', width, 'x', height);

        this.loadStarsBackground();
        this.setupControls();
        this.setupPlanets();

        window.addEventListener('resize', () => this.onWindowResize());
        this.animate();
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

        if (planetData.texturePath) {
            state.setTexture(planetData.texturePath);
        }

        const planet = new Planet(
            pos,
            vel,
            angularVelocity,
            planetData.name || `Planet #${this.planets.length}`,
            state,
        );

        this.planets.push(planet);
        this.orbitTrails.set(planet, []);
        this.createPlanetMesh(planet);

        this.updatePlanetPositions();
        if (this.selectedPlanet) {
            this.selectedPlanet.clicked();
        }
        this.selectedPlanet = planet;
        planet.clicked();
        this.cameraDistance = this.getMinCameraDistanceFor(planet);
        this.updateCameraPosition();
    }

    /** Returns the camera position in physics world coordinates (meters). */
    getCameraPhysicsPosition() {
        if (!this.camera) return { x: 0, y: 0, z: 0 };
        return {
            x: this.camera.position.x / this.displayScale,
            y: this.camera.position.y / this.displayScale,
            z: this.camera.position.z / this.displayScale
        };
    }

    clearSimulation() {
        for (const planet of this.planets) {
            this.disposePlanetMesh(planet);
        }
        if (this.disposeOrbitLines) this.disposeOrbitLines();
        if (this.selectedPlanet) {
            this.selectedPlanet.clicked();
        }
        this.selectedPlanet = null;
        this.planets = [];
        this.orbitTrails.clear();
        this.updateCameraPosition();
    }

    selectPlanet(planet) {
        if (!planet || !this.planets.includes(planet)) return;
        if (this.selectedPlanet === planet) return;
        if (this.selectedPlanet) this.selectedPlanet.clicked();
        this.selectedPlanet = planet;
        planet.clicked();

        for (const p of this.planets) {
            const trail = this.orbitTrails.get(p);
            if (trail) trail.length = 0;
            else this.orbitTrails.set(p, []);
        }

        this.cameraDistance = this.getMinCameraDistanceFor(planet);
        this.cameraOffsetX = this.cameraOffsetY = this.cameraOffsetZ = 0;
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
        this.orbitTrails.delete(planet);
        if (this.disposeOrbitLineForPlanet) this.disposeOrbitLineForPlanet(planet);
        this.planets.splice(idx, 1);
        this.updateCameraPosition();
    }

    setupPlanets() {
        const sunState = new State(1.9885e30, 6.957e8, 5778.0, this.consts);
        const earthState = new State(5.972e24, 6.371e6, 288, this.consts);
        const sun = new Planet(
            new Vector([0.0, 0.0, 0.0]),
            new Vector([0.0, 0.0, 0.0]),
            2.963e-6, 'Sun', sunState
        );
        const earthOrbitRadius = 1.496e11;
        const earth = new Planet(
            new Vector([earthOrbitRadius, 0.0, 0.0]),
            new Vector([0.0, 29780, 0.0]),
            7.272e-5, 'Earth', earthState
        );

        this.planets.push(sun);
        this.planets.push(earth);
        for (const p of this.planets) this.orbitTrails.set(p, []);

        for (const planet of this.planets) {
            this.createPlanetMesh(planet);
        }

        this.updatePlanetPositions();
        this.selectedPlanet = sun;
        sun.clicked();
        this.cameraDistance = this.getMinCameraDistanceFor(sun);
        this.updateCameraPosition();

        console.log('Planets created:', this.planets.length);
    }

    update(deltaTime) {
        if (this.isPaused) return;

        if (!this.selectedPlanet && this.planets.length > 0) {
            this.selectedPlanet = this.planets[0];
            this.selectedPlanet.clicked();
            this.cameraDistance = this.getMinCameraDistanceFor(this.selectedPlanet);
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

        for (const planet of toRemove) {
            const index = this.planets.indexOf(planet);
            if (index > -1) {
                this.disposePlanetMesh(planet);
                this.orbitTrails.delete(planet);
                if (this.disposeOrbitLineForPlanet) this.disposeOrbitLineForPlanet(planet);
                this.planets.splice(index, 1);
            }
        }

        for (const merged of toAdd) {
            this.planets.push(merged);
            this.orbitTrails.set(merged, []);
            this.createPlanetMesh(merged);
            if (this.selectedPlanet && (toRemove.includes(this.selectedPlanet))) {
                this.selectedPlanet = merged;
                merged.clicked();
                this.cameraDistance = this.getMinCameraDistanceFor(merged);
            }
        }

        if (this.selectedPlanet && !this.planets.includes(this.selectedPlanet)) {
            this.selectedPlanet = null;
        }

        for (const planet of this.planets) {
            planet.updatePosition(deltaTime, this.timeFactor);
        }

        this.updateCameraPosition();
        this.updatePlanetPositions();
    }

    animate() {
        this.animationFrameId = requestAnimationFrame(() => this.animate());
        this.update(this.deltaTime);
        this.render();
    }

    setG(pct) { this.consts.G = (pct / 100) * 6.67430e-11; }
    setC(pct) { this.consts.c = (pct / 100) * 299792458; }
    setSigma(pct) { this.consts.σ = (pct / 100) * 5.67e-8; }
    setTimeFactor(value) { this.timeFactor = value; }
    setBounce(enabled) { this.bounce = enabled; }
    setRK4(enabled) { this.useRK4 = enabled; }

    dispose() {
        if (this.animationFrameId) {
            cancelAnimationFrame(this.animationFrameId);
        }
        this.clearSimulation();
        if (this.renderer) {
            this.renderer.dispose();
        }
    }
}
