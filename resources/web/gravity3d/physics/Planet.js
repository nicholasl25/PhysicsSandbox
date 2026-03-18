/**
 * Planet class - represents a celestial body in the gravity simulation.
 * Uses Vector for position and velocity to support 3D simulations.
 */
class Planet {
    /**
     * @param {Vector} pos Position (m)
     * @param {Vector} vel Velocity (m/s)
     * @param {number} angularVelocity Angular velocity (rad/s)
     * @param {string} name Display name
     * @param {State} state Physical state (mass kg, radius m, temperature K, etc.)
     */
    constructor(pos, vel, angularVelocity, name, state) {
    
        this.pos = pos.clone();
        this.vel = vel.clone();
        this.isSelected = false;
        this.angularVelocity = angularVelocity || 0.0;
        this.name = name || state.getType();
        this.rotationAngle = 0.0;
        this.state = state
        
        // Three.js objects (will be set by the renderer)
        this.mesh = null;
        this.texture = null;
    }
    
    /**
     * Advance position + rotation by the simulation time step.
     * @param {number} deltaTime Render dt (s)
     * @param {number} timeFactor Simulation time multiplier (s of sim per real second)
     */
    updatePosition(deltaTime, timeFactor) {
        const displacement = this.vel.multiply(deltaTime * timeFactor);
        this.pos = this.pos.add(displacement);
        
        // Update rotation angle
        this.rotationAngle += this.angularVelocity * timeFactor * deltaTime;
        this.rotationAngle = this.rotationAngle % (Math.PI * 2);
        if (this.rotationAngle > 0) {
            this.rotationAngle -= Math.PI * 2;
        }
    }
    
    /**
     * Integrate velocity from acceleration.
     * @param {Vector} accel Acceleration (m/s^2)
     * @param {number} deltaTime Time step (s)
     */
    updateVelocity(accel, deltaTime) {
        const deltaVel = accel.multiply(deltaTime);
        this.vel = this.vel.add(deltaVel);
    }
    
    /**
     * @param {Planet} other
     * @returns {number} Distance (m)
     */
    distanceTo(other) {
        const delta = this.pos.subtract(other.pos);
        return delta.magnitude();
    }
    
    /**
     * @param {Planet} other
     * @param {{G:number}} consts { G } where `G` is gravitational constant
     * @returns {Vector} Force on this due to `other` (N = kg·m/s^2)
     */
    gravitationalForceFrom(other, consts) {
        const distance = this.distanceTo(other);
        if (distance === 0.0) {
            return new Vector(new Array(this.pos.dimensions()).fill(0));
        }

        const m1 = this.state.getMass();
        const m2 = other.state.getMass();
        
        const direction = other.pos.subtract(this.pos).normalize();
        const forceMagnitude = consts.G * m1 * m2 / (distance * distance);
        
        return direction.multiply(forceMagnitude);
    }
    
    /**
     * @param {Planet} other
     * @returns {boolean} True if spheres overlap
     */
    collidesWith(other) {
        return this.distanceTo(other) < (this.state.getRadius() + other.state.getRadius());
    }
    
    /**
     * @param {Planet} other
     * @param {{G:number}} consts (currently forwarded to State)
     * @returns {Planet} New merged planet (pos m, vel m/s)
     */
    merge(other, consts) {
        const combinedMass = this.state.getMass() + other.state.getMass();
        
        // Weighted average of velocities
        let newVel = this.vel.multiply(this.state.getMass()).add(other.vel.multiply(other.state.getMass()));
        newVel = newVel.multiply(1.0 / combinedMass);
        
        // Weighted average of positions
        let newPos = this.pos.multiply(this.state.getMass()).add(other.pos.multiply(other.state.getMass()));
        newPos = newPos.multiply(1.0 / combinedMass);
        
        const newTemperature = (this.state.getTemperature() * this.state.getMass() + other.state.getTemperature() * other.state.getMass()) / combinedMass;
        const newRadius = Math.max(this.state.getRadius(), other.state.getRadius());
        
        // Average colors
        const c1 = this.state.getColor() || new THREE.Color(0.3, 0.5, 1.0);
        const c2 = other.state.getColor() || new THREE.Color(0.3, 0.5, 1.0);
        const newColor = new THREE.Color(
            (c1.r + c2.r) / 2,
            (c1.g + c2.g) / 2,
            (c1.b + c2.b) / 2
        );
        
        const momentOfInertiaCoeff = 0.4;
        const angularMomentum = momentOfInertiaCoeff * (
            this.state.getRadius() * this.state.getRadius() * this.state.getMass() * this.angularVelocity +
            other.state.getRadius() * other.state.getRadius() * other.state.getMass() * other.angularVelocity
        );
        const newAngularVelocity = angularMomentum / (momentOfInertiaCoeff * newRadius * newRadius * combinedMass);
        
        // Use texture and name from larger planet
        let newTexturePath = null;
        let newName = null;
        if (this.state.getRadius() > other.state.getRadius()) {
            newTexturePath = this.state.getTexturepath() || other.state.getTexturepath();
            newName = this.name;
        } else {
            newTexturePath = other.state.getTexturepath() || this.state.getTexturepath();
            newName = other.name;
        }
        
        // Create new state for merged planet
        const newState = new State(combinedMass, newRadius, newTemperature, consts);
        newState.texturepath = newTexturePath;
        newState.color = newColor;

        return new Planet(
            newPos,
            newVel,
            newAngularVelocity,
            newName,
            newState
        );
    }
    
    /**
     * @param {number} coefficientOfRestitution e (1 = perfectly elastic)
     * @param {Planet} other
     */
    bouncePlanet(coefficientOfRestitution, other) {
        const deltaPos = other.pos.subtract(this.pos);
        const deltaPosMag = deltaPos.magnitude();
        if (deltaPosMag === 0.0) return;
        
        const n = deltaPos.multiply(1.0 / deltaPosMag);
        
        // Project velocities onto the collision normal
        const u1 = this.vel.dot(n);
        const u2 = other.vel.dot(n);
        
        const relVel = u1 - u2;
        if (relVel <= 0) return; // they are separating, no bounce
        
        const m1 = this.state.getMass();
        const m2 = other.state.getMass();
        const e = coefficientOfRestitution;
        
        const u1p = ((m1 - e * m2) * u1 + (1 + e) * m2 * u2) / (m1 + m2);
        const u2p = ((m2 - e * m1) * u2 + (1 + e) * m1 * u1) / (m1 + m2);
        
        const deltaU1 = u1p - u1;
        const deltaU2 = u2p - u2;
        
        // Update velocities along collision normal
        const deltaVel1 = n.multiply(deltaU1);
        const deltaVel2 = n.multiply(deltaU2);
        
        this.vel = this.vel.add(deltaVel1);
        other.vel = other.vel.add(deltaVel2);
    }


    /**
     * @param {Planet} other Radiating source planet
     * @param {number} scaledDt Time step (s)
     */
    applyRadiationFrom(other, scaledDt) {
        const solarLuminosity = 3.83e26;
        const R = this.state.getRadius(); // m
        const r = this.distanceTo(other);
        const L_W = other.state.getLuminosity() * solarLuminosity; // W

        // Approx value of albedo for planets
        const albedo = 0.3;
        const transferPower = (1 - albedo) * L_W * R * R / (4 * r * r);
        const energyTransferred = transferPower * scaledDt;

        this.updateInternalEnergy(energyTransferred);
    }

    /**
     * @param {number} scaledDt Time step (s)
     */
    applyRadiationAway(scaledDt) {
        const σ = this.state.consts.σ;
        const T = this.state.getTemperature();
        const R = this.state.getRadius();
        // Approx value of emissivity for IR object
        const emissivity = 0.9;
        const powerRadiated = emissivity * σ * 4 * Math.PI * R * R * Math.pow(T, 4);
        const energyLost = powerRadiated * scaledDt;
        this.updateInternalEnergy(-energyLost);
    }


    /**
     * @param {number} change Energy change (J)
     */
    updateInternalEnergy(change) {
        const C = this.state.getHeatCap();
        if (C === 0) return;
        const M = this.state.getMass(); // kg
        const fraction = this.state.getCoolingMassFraction();
        const deltaT = change / (C * M * fraction);
        this.state.updateTemp(deltaT);
    }
    
    /**
     * Toggle selection state used by renderer/UI.
     */
    clicked() {
        this.isSelected = !this.isSelected;
    }
    
    // Getters
    /**
     * @returns {Vector} Velocity (m/s)
     */
    getVelocity() { return this.vel.clone(); }
    /**
     * @returns {Vector} Position (m)
     */
    getPosition() { return this.pos.clone(); }

    /**
     * @returns {State} Underlying physical state (mass/radius/temperature/etc.)
     */
    getState() { return this.state; }
    /**
     * @returns {boolean} Whether this planet is selected
     */
    isClicked() { return this.isSelected; }
    /**
     * @returns {number} Angular velocity (rad/s)
     */
    getAngularVelocity() { return this.angularVelocity; }
    /**
     * @returns {string} Planet display name
     */
    getName() { return this.name; }
    /**
     * @returns {number} Rotation angle (rad)
     */
    getRotationAngle() { return this.rotationAngle; }
    // (intentionally no direct getRadius/getMass/etc. wrappers; use getState())
    
    // Setters
    /**
     * @param {Vector} newVel Velocity (m/s)
     */
    setVelocity(newVel) { this.vel = newVel.clone(); }
    /**
     * @param {Vector} newPos Position (m)
     */
    setPosition(newPos) { this.pos = newPos.clone(); }
}
