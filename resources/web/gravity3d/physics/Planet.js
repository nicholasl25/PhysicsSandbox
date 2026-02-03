/**
 * Planet class - represents a celestial body in the gravity simulation.
 * Uses Vector for position and velocity to support 3D simulations.
 */
class Planet {
    constructor(pos, vel, angularVelocity, name, state) {
    
        this.pos = pos.clone();
        this.vel = vel.clone();
        this.isSelected = false;
        this.angularVelocity = angularVelocity || 0.0;
        this.name = name || 'Planet';
        this.rotationAngle = 0.0;
        this.state = state
        
        // Three.js objects (will be set by the renderer)
        this.mesh = null;
        this.texture = null;
    }
    
    /**
     * Updates the planet's position based on its velocity.
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
     * Updates the planet's velocity based on acceleration vector.
     */
    updateVelocity(accel, deltaTime) {
        const deltaVel = accel.multiply(deltaTime);
        this.vel = this.vel.add(deltaVel);
    }
    
    /**
     * Calculates the distance to another planet.
     */
    distanceTo(other) {
        const delta = this.pos.subtract(other.pos);
        return delta.magnitude();
    }
    
    /**
     * Calculates gravitational force exerted on this planet by another planet.
     * Formula: F = G * m1 * m2 / r²
     */
    gravitationalForceFrom(other, gravitationalConstant) {
        const distance = this.distanceTo(other);
        if (distance === 0.0) {
            return new Vector(new Array(this.pos.dimensions()).fill(0));
        }

        const m1 = this.state.getMass();
        const m2 = other.state.getMass();
        
        const direction = other.pos.subtract(this.pos).normalize();
        const forceMagnitude = gravitationalConstant * m1 * m2 / (distance * distance);
        
        return direction.multiply(forceMagnitude);
    }
    
    /**
     * Checks if this planet collides with another planet.
     */
    collidesWith(other) {
        return this.distanceTo(other) < (this.state.getRadius() + other.state.getRadius());
    }
    
    /**
     * Merges this planet with another planet.
     */
    merge(other) {
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
        const newState = new State(combinedMass, newRadius, newTemperature);
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
     * Bounces this planet off another planet.
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
     * Toggles selection state.
     */
    clicked() {
        this.isSelected = !this.isSelected;
    }
    
    // Getters
    getVelocity() { return this.vel.clone(); }
    getPosition() { return this.pos.clone(); }
    isClicked() { return this.isSelected; }
    getAngularVelocity() { return this.angularVelocity; }
    getName() { return this.name; }
    getRotationAngle() { return this.rotationAngle; }
    getRadius() { return this.state.getRadius(); }
    getMass() { return this.state.getMass(); }
    getTemperature() { return this.state.getTemperature(); }
    getColor() { return this.state.getColor(); }
    getTexturepath() { return this.state.getTexturepath(); }
    
    // Setters
    setVelocity(newVel) { this.vel = newVel.clone(); }
    setPosition(newPos) { this.pos = newPos.clone(); }
}
