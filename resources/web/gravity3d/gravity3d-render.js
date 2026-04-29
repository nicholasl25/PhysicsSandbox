/**
 * Gravity3D Web Simulation - Render
 * Camera, controls, textures, meshes, markers, trails, resize.
 * Extends Gravity3DSimulation prototype.
 */

(function () {
    'use strict';

    Gravity3DSimulation.prototype.loadStarsBackground = function () {
        this.textureLoader.load('/textures/Stars.png', (texture) => {
            texture.mapping = THREE.EquirectangularReflectionMapping;
            this.scene.background = texture;
        }, undefined, (error) => {
            console.warn('Could not load stars background:', error);
        });
    };

    Gravity3DSimulation.prototype.setupControls = function () {
        this.renderer.domElement.addEventListener('mousedown', (e) => {
            if (e.button === 0) {
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
                const maxPitch = Math.PI / 2 - 0.1;
                this.cameraPitch = Math.max(-maxPitch, Math.min(maxPitch, this.cameraPitch));
                this.lastMouseX = e.clientX;
                this.lastMouseY = e.clientY;
                this.updateCameraPosition();
            }
        });

        this.renderer.domElement.addEventListener('mouseup', (e) => {
            if (e.button === 0) this.isDragging = false;
        });

        this.renderer.domElement.addEventListener('mouseleave', () => {
            this.isDragging = false;
        });

        document.addEventListener('keydown', (e) => {
            if (e.code === 'Space' && e.target === document.body) {
                e.preventDefault();
                this.isPaused = !this.isPaused;
            }
        });

        this.renderer.domElement.addEventListener('wheel', (e) => {
            e.preventDefault();
            const zoomFactor = Math.pow(1.1, -e.deltaY / 100);
            this.cameraDistance *= zoomFactor;
            const minDistance = this.selectedPlanet ? this.getMinCameraDistanceFor(this.selectedPlanet) : 0;
            this.cameraDistance = Math.max(minDistance, this.cameraDistance);
            this.updateCameraPosition();
        });
    };

    Gravity3DSimulation.prototype.updateCameraPosition = function () {
        if (!this.selectedPlanet || this.planets.length === 0) {
            const cosPitch = Math.cos(this.cameraPitch);
            const sinPitch = Math.sin(this.cameraPitch);
            const cosYaw = Math.cos(this.cameraYaw);
            const sinYaw = Math.sin(this.cameraYaw);
            const offsetX = this.cameraDistance * cosPitch * sinYaw * this.displayScale;
            const offsetY = this.cameraDistance * sinPitch * this.displayScale;
            const offsetZ = -this.cameraDistance * cosPitch * cosYaw * this.displayScale;
            this.camera.position.set(offsetX + this.cameraOffsetX, offsetY + this.cameraOffsetY, offsetZ + this.cameraOffsetZ);
            this.camera.lookAt(this.cameraOffsetX, this.cameraOffsetY, this.cameraOffsetZ);
            return;
        }
        const cosPitch = Math.cos(this.cameraPitch);
        const sinPitch = Math.sin(this.cameraPitch);
        const cosYaw = Math.cos(this.cameraYaw);
        const sinYaw = Math.sin(this.cameraYaw);
        const offsetX = this.cameraDistance * cosPitch * sinYaw * this.displayScale;
        const offsetY = this.cameraDistance * sinPitch * this.displayScale;
        const offsetZ = -this.cameraDistance * cosPitch * cosYaw * this.displayScale;
        this.camera.position.set(offsetX + this.cameraOffsetX, offsetY + this.cameraOffsetY, offsetZ + this.cameraOffsetZ);
        this.camera.lookAt(this.cameraOffsetX, this.cameraOffsetY, this.cameraOffsetZ);
    };

    Gravity3DSimulation.prototype.updateCameraFrustum = function () {
        if (!this.camera || this.planets.length === 0) return;
        const cam = this.camera.position;
        let minDist = Infinity;
        let maxDist = 0;
        for (const planet of this.planets) {
            if (!planet.mesh) continue;
            const state = planet.getState();
            const rScene = Math.max(state.getRadius() * this.displayScale, 0.01);
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
    };

    Gravity3DSimulation.prototype.getMinCameraDistanceFor = function (planet) {
        const state = planet.getState();
        const visualRadiusScene = Math.max(state.getRadius() * this.displayScale, 0.01);
        return (4 * visualRadiusScene) / this.displayScale;
    };

    Gravity3DSimulation.prototype.loadTexture = function (texturePath, callback) {
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
    };

    Gravity3DSimulation.prototype.createGrayscaleTexture = function (threeTexture) {
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
    };

    Gravity3DSimulation.prototype.getStarHaloTexture = function () {
        if (this.starHaloTexture) return this.starHaloTexture;
        const size = 512;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const ctx = canvas.getContext('2d');
        const cx = size / 2;
        const cy = size / 2;
        const r = size / 2;
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
    };

    Gravity3DSimulation.prototype.disposePlanetMesh = function (planet) {
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
    };

    Gravity3DSimulation.prototype.addStarLightAndHalo = function (mesh, planet, radiusScene, starColor) {
        const state = planet.getState();
        const intensity = state.getLuminosity();
        const pointLight = new THREE.PointLight(starColor.getHex(), intensity, 0);
        pointLight.distance = 0;
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
        const LuminosityPerArea = state.getLuminosityPerArea();
        const illuminationScore = Math.min(Math.max(2 * Math.log2(LuminosityPerArea) + 20, 0), 100);
        const haloDiameter = radiusScene * illuminationScore;
        halo.scale.set(haloDiameter, haloDiameter, 1);
        halo.renderOrder = 1;
        halo.frustumCulled = false;
        mesh.add(halo);
        planet.starHalo = halo;
    };

    Gravity3DSimulation.prototype.createPlanetMesh = function (planet) {
        const state = planet.getState();
        const radiusScene = Math.max(state.getRadius() * this.displayScale, 0.01);
        const geometry = new THREE.SphereGeometry(radiusScene, 32, 16);
        const isStar = planet.state.type === PlanetTypes.STAR;
        const defaultColor = planet.state.getColor() || new THREE.Color(0.3, 0.5, 1.0);
        let material = new THREE.MeshStandardMaterial({
            color: defaultColor,
            emissive: isStar ? defaultColor.clone() : new THREE.Color(0x000000),
            emissiveIntensity: isStar ? 0.8 : 0,
        });
        const mesh = new THREE.Mesh(geometry, material);
        planet.mesh = mesh;
        this.scene.add(mesh);

        if (isStar) {
            const starColor = planet.state.getColor() || defaultColor;
            this.addStarLightAndHalo(mesh, planet, radiusScene, starColor);
        }

        if (planet.state.getTexturepath()) {
            const tintColor = planet.state.getColor();
            const isStarType = planet.state.type === PlanetTypes.STAR;
            this.loadTexture(planet.state.getTexturepath(), (texture) => {
                if (texture) {
                    if (isStarType && tintColor) {
                        const grayscaleMap = this.createGrayscaleTexture(texture);
                        material = new THREE.MeshStandardMaterial({
                            map: grayscaleMap,
                            color: tintColor,
                            emissive: tintColor.clone(),
                            emissiveIntensity: 0.5,
                        });
                    } else {
                        material = new THREE.MeshStandardMaterial({ map: texture });
                    }
                    mesh.material = material;
                }
            });
        }
        return mesh;
    };

    Gravity3DSimulation.prototype.updatePlanetPositions = function () {
        let originX = 0, originY = 0, originZ = 0;
        if (this.selectedPlanet) {
            const originPos = this.selectedPlanet.getPosition();
            originX = originPos.get(0);
            originY = originPos.get(1);
            originZ = originPos.dimensions() > 2 ? originPos.get(2) : 0;
        }
        for (const planet of this.planets) {
            if (!planet.mesh) continue;
            const pos = planet.getPosition();
            const x = (pos.get(0) - originX) * this.displayScale;
            const y = (pos.get(1) - originY) * this.displayScale;
            const z = ((pos.dimensions() > 2 ? pos.get(2) : 0) - originZ) * this.displayScale;
            planet.mesh.position.set(x, y, z);
            planet.mesh.rotation.y = planet.getRotationAngle();
        }
    };

    Gravity3DSimulation.prototype.render = function () {
        if (!this.renderer || !this.scene || !this.camera) return;
        this.updateCameraFrustum();
        this.renderer.render(this.scene, this.camera);
        this.recordOrbitTrails();
        this.drawObjectMarkers();
    };

    Gravity3DSimulation.prototype.recordOrbitTrails = function () {
        for (const planet of this.planets) {
            if (!planet.mesh) continue;
            let trail = this.orbitTrails.get(planet);
            if (!trail) {
                trail = [];
                this.orbitTrails.set(planet, trail);
            }
            trail.push(planet.mesh.position.clone());
            if (trail.length > this.orbitTrailMaxLength) trail.shift();

            // Keep depth-tested 3D orbit line in sync with the stored trail.
            this.updateOrbitLineForPlanet(planet, trail);
        }
    };

    Gravity3DSimulation.prototype.updateOrbitLineForPlanet = function (planet, trail) {
        if (!this.scene) return;

        let lineObj = this.orbitLines?.get(planet);
        if (!lineObj) {
            const geometry = new THREE.BufferGeometry();
            const material = new THREE.LineBasicMaterial({
                color: 0xffffff,
                transparent: true,
                opacity: 0.45,
                depthTest: true,
                depthWrite: false,
            });
            const line = new THREE.Line(geometry, material);
            line.frustumCulled = false;
            lineObj = { line, geometry, material };
            this.orbitLines.set(planet, lineObj);
            this.scene.add(line);
        }

        if (!trail || trail.length < 2) {
            lineObj.line.visible = false;
            return;
        }

        const positions = new Float32Array(trail.length * 3);
        for (let i = 0; i < trail.length; i++) {
            const p = trail[i];
            positions[i * 3] = p.x;
            positions[i * 3 + 1] = p.y;
            positions[i * 3 + 2] = p.z;
        }
        lineObj.geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        lineObj.line.visible = true;
    };

    // Orbit line lifecycle helpers (render owns how orbit line objects are created/disposed)
    Gravity3DSimulation.prototype.disposeOrbitLineForPlanet = function (planet) {
        if (!this.scene || !this.orbitLines) return;
        const lineObj = this.orbitLines.get(planet);
        if (!lineObj) return;
        if (lineObj.line) this.scene.remove(lineObj.line);
        if (lineObj.geometry) lineObj.geometry.dispose();
        if (lineObj.material) lineObj.material.dispose();
        this.orbitLines.delete(planet);
    };

    Gravity3DSimulation.prototype.disposeOrbitLines = function () {
        if (!this.orbitLines) return;
        // Copy values because disposeOrbitLineForPlanet mutates the map.
        const planets = Array.from(this.orbitLines.keys());
        for (const p of planets) this.disposeOrbitLineForPlanet(p);
    };

    Gravity3DSimulation.prototype.onWindowResize = function () {
        const width = this.container.clientWidth;
        const height = this.container.clientHeight;
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(width, height);
        if (this.markerCanvas) {
            this.markerCanvas.width = width;
            this.markerCanvas.height = height;
        }
    };

    Gravity3DSimulation.prototype.drawObjectMarkers = function () {
        if (!this.markerCanvas || !this.markerCtx || !this.camera) return;
        const w = this.markerCanvas.width;
        const h = this.markerCanvas.height;
        this.markerCtx.clearRect(0, 0, w, h);
        if (this.planets.length === 0) return;

        const minRadius = 4;
        const selectedRadius = 6;
        for (const planet of this.planets) {
            if (!planet.mesh) continue;
            const state = planet.getState();
            this.projectVector.set(planet.mesh.position.x, planet.mesh.position.y, planet.mesh.position.z);
            this.projectVector.project(this.camera);
            if (this.projectVector.z > 1) continue;
            const px = (this.projectVector.x + 1) * 0.5 * w;
            const py = (1 - this.projectVector.y) * 0.5 * h;
            const markerRadius = planet === this.selectedPlanet ? selectedRadius * 2 : minRadius * 2;
            const radiusScene = Math.max(state.getRadius() * this.displayScale, 0.01);
            this._viewDir.subVectors(this.camera.position, planet.mesh.position).normalize();
            this._upPerp.copy(this.camera.up).addScaledVector(this._viewDir, -this.camera.up.dot(this._viewDir));
            if (this._upPerp.lengthSq() < 1e-10) {
                this._upPerp.crossVectors(this._viewDir, this._axisX);
                if (this._upPerp.lengthSq() < 1e-10) this._upPerp.crossVectors(this._viewDir, this._axisY);
                this._upPerp.normalize();
            } else {
                this._upPerp.normalize();
            }
            this._radiusPoint.copy(planet.mesh.position).addScaledVector(this._upPerp, radiusScene);
            this.projectVector.set(this._radiusPoint.x, this._radiusPoint.y, this._radiusPoint.z).project(this.camera);
            const qx = (this.projectVector.x + 1) * 0.5 * w;
            const qy = (1 - this.projectVector.y) * 0.5 * h;
            const apparentRadiusPx = Math.sqrt((qx - px) ** 2 + (qy - py) ** 2);
            if (apparentRadiusPx * 2 < markerRadius) {
                this.markerCtx.beginPath();
                this.markerCtx.arc(px, py, markerRadius, 0, Math.PI * 2);
                this.markerCtx.strokeStyle = 'rgba(255,255,255,0.7)';
                this.markerCtx.lineWidth = 2;
                this.markerCtx.stroke();
            }
            const labelOffset = Math.max(markerRadius, apparentRadiusPx) + 10;
            this.markerCtx.font = '12px sans-serif';
            this.markerCtx.fillStyle = 'rgba(255,255,255,0.9)';
            this.markerCtx.textBaseline = 'middle';
            this.markerCtx.textAlign = 'left';
            this.markerCtx.fillText(planet.getName(), px + labelOffset, py);
        }
    };
})();
