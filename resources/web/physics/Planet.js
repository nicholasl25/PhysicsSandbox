/**
 * Planet class - represents a celestial body in the gravity simulation.
 * Uses Vector for position and velocity to support 3D simulations.
 */
class Planet {
    constructor(dimension, mass, radius, pos, vel, angularVelocity, temperature, color, texturePath, name) {
        if (dimension !== 2 && dimension !== 3) {
            throw new Error('Dimension must be 2 or 3');
        }
        if (pos.dimensions() !== dimension || vel.dimensions() !== dimension) {
            throw new Error('Position and velocity vectors must match dimension: ' + dimension);
        }
        
        this.dimension = dimension;
        this.mass = mass;
        this.radius = radius;
        this.pos = pos.clone();
        this.vel = vel.clone();
        this.color = color || { r: 0.3, g: 0.5, b: 1.0 };
        this.isSelected = false;
        this.texturePath = texturePath;
        this.angularVelocity = angularVelocity || 0.0;
        this.temperature = temperature || 300.0;
        this.name = name || 'Planet';
        this.rotationAngle = 0.0;
        
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
        if (this.rotationAngle < 0) {
            this.rotationAngle += Math.PI * 2;
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
        
        const direction = other.pos.subtract(this.pos).normalize();
        const forceMagnitude = gravitationalConstant * this.mass * other.mass / (distance * distance);
        
        return direction.multiply(forceMagnitude);
    }
    
    /**
     * Checks if this planet collides with another planet.
     */
    collidesWith(other) {
        return this.distanceTo(other) < (this.radius + other.radius);
    }
    
    /**
     * Merges this planet with another planet.
     */
    merge(other) {
        const combinedMass = this.mass + other.mass;
        
        // Weighted average of velocities
        let newVel = this.vel.multiply(this.mass).add(other.vel.multiply(other.mass));
        newVel = newVel.multiply(1.0 / combinedMass);
        
        // Weighted average of positions
        let newPos = this.pos.multiply(this.mass).add(other.pos.multiply(other.mass));
        newPos = newPos.multiply(1.0 / combinedMass);
        
        const newTemperature = (this.temperature * this.mass + other.temperature * other.mass) / combinedMass;
        const newRadius = Math.max(this.radius, other.radius);
        
        // Average colors
        const c1 = this.color;
        const c2 = other.color;
        const newColor = {
            r: (c1.r + c2.r) / 2,
            g: (c1.g + c2.g) / 2,
            b: (c1.b + c2.b) / 2
        };
        
        const momentOfInertiaCoeff = 0.4; // Sphere
        const angularMomentum = momentOfInertiaCoeff * (
            this.radius * this.radius * this.mass * this.angularVelocity +
            other.radius * other.radius * other.mass * other.angularVelocity
        );
        const newAngularVelocity = angularMomentum / (momentOfInertiaCoeff * newRadius * newRadius * combinedMass);
        
        // Use texture and name from larger planet
        let newTexturePath = null;
        let newName = null;
        if (this.radius > other.radius) {
            newTexturePath = this.texturePath || other.texturePath;
            newName = this.name;
        } else {
            newTexturePath = other.texturePath || this.texturePath;
            newName = other.name;
        }
        
        return new Planet(
            this.dimension,
            combinedMass,
            newRadius,
            newPos,
            newVel,
            newAngularVelocity,
            newTemperature,
            newColor,
            newTexturePath,
            newName
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
        
        const m1 = this.mass;
        const m2 = other.mass;
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
    getMass() { return this.mass; }
    getRadius() { return this.radius; }
    getColor() { return this.color; }
    isClicked() { return this.isSelected; }
    getAngularVelocity() { return this.angularVelocity; }
    getTemperature() { return this.temperature; }
    getName() { return this.name; }
    getRotationAngle() { return this.rotationAngle; }
    
    // Setters
    setVelocity(newVel) { this.vel = newVel.clone(); }
    setPosition(newPos) { this.pos = newPos.clone(); }
}
