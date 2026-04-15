/**
 * Gravity3D Web Simulation - Physics
 * Gravity, radiation, collisions/merge. Extends Gravity3DSimulation prototype.
 */

(function () {
    'use strict';

    /**
     * Resolve overlap between two planets: bounce or merge into pendingAdd / pendingRemove.
     * @param {Planet} planet
     * @param {Planet} other
     * @returns {boolean} true if a collision was handled (caller should stop processing this planet pair)
     */
    Gravity3DSimulation.prototype.handleCollision = function (planet, other) {
        if (!planet.collidesWith(other)) return false;
        if (this.bounce) {
            planet.bouncePlanet(this.coefficientOfRestitution, other);
        } else {
            const merged = planet.merge(other, this.consts);
            this.pendingAdd.push(merged);
            this.pendingRemove.push(planet);
            this.pendingRemove.push(other);
        }
        return true;
    };

    /**
     * Gravitational acceleration (m/s²) at a point. Bodies whose sphere contains the point
     * (distance from body center strictly inside the body's radius) do not contribute.
     * @param {Vector} position point in world space (m)
     * @param {Planet} [excludeBody] if set, this mass is omitted (needed for RK4 trial points where `position` is not that body’s center).
     * @returns {Vector} acceleration vector
     */
    Gravity3DSimulation.prototype.gravitationalAccelerationAt = function (position, excludeBody) {
        let acc = new Vector([0.0, 0.0, 0.0]);
        const G = this.consts.G;
        for (const body of this.planets) {
            if (excludeBody !== undefined && body === excludeBody) continue;
            const delta = body.pos.subtract(position);
            const d = delta.magnitude();
            if (d === 0.0) continue;
            const R = body.state.getRadius();
            if (d < R) continue;
            const m2 = body.state.getMass();
            const n = delta.multiply(1.0 / d);
            const aMag = G * m2 / (d * d);
            acc = acc.add(n.multiply(aMag));
        }
        return acc;
    };

    /**
     * For one planet: bounce or merge. Gravity is applied once per step in applyGravitationalAccelerationAll().
     */
    Gravity3DSimulation.prototype.resolveCollisionsForPlanet = function (planet) {
        if (this.pendingRemove.includes(planet)) return;
        for (const other of this.planets) {
            if (planet === other) continue;
            if (this.pendingRemove.includes(other)) continue;
            if (this.handleCollision(planet, other)) return;
        }
    };

    /**
     * Set planet.acceleration to gravitational field a(t) at each body’s center (after removals/merges).
     * All integrators use this as the acceleration for the current substep.
     */
    Gravity3DSimulation.prototype.applyGravitationalAccelerationAll = function () {
        for (const planet of this.planets) {
            planet.acceleration = this.gravitationalAccelerationAt(planet.pos, planet);
        }
    };

    // Euler iterate all planets in this.planets
    Gravity3DSimulation.prototype.eulerUpdateAll = function (scaledDt) {
        for (const planet of this.planets) {
            planet.eulerUpdate(scaledDt);
        }
    };

    Gravity3DSimulation.prototype.semiEulerUpdateAll = function (scaledDt) {
        for (const planet of this.planets) {
            planet.semiEulerUpdate(scaledDt);
        }
    };

    /**
     * Velocity Verlet: all positions from v and a(t), then all v from a(t) and a(t+dt).
     * Requires each planet.acceleration === a(t) at current position before this runs.
     * Gravity uses this.planets only—the same set Euler and semiEuler advance after removals and merges.
     * @param {number} scaledDt simulation timestep (s)
     */
    Gravity3DSimulation.prototype.verletUpdateAll = function (scaledDt) {
        for (const planet of this.planets) {
            planet.verletPositionUpdate(scaledDt);
        }
        for (const planet of this.planets) {
            const newAcceleration = this.gravitationalAccelerationAt(planet.pos, planet);
            planet.verletVelocityUpdate(newAcceleration, scaledDt);
        }
    };

    /**
     * RK4 per planet. Passes excludeBody into acceleration so trial points do not feel this body’s own mass
     * (body.pos stays at step start while sampling rHalf1, etc.).
     * @param {number} scaledDt simulation timestep (s)
     */
    Gravity3DSimulation.prototype.rk4UpdateAll = function (scaledDt) {
        const halfDt = scaledDt * 0.5;
        for (const planet of this.planets) {
            const r0 = planet.getPosition();
            const v0 = planet.getVelocity();

            const kV1 = this.gravitationalAccelerationAt(r0, planet);
            const kR1 = v0;

            const rHalf1 = r0.add(kR1.multiply(halfDt));
            const kV2 = this.gravitationalAccelerationAt(rHalf1, planet);
            const kR2 = v0.add(kV1.multiply(halfDt));

            const rHalf2 = r0.add(kR2.multiply(halfDt));
            const kV3 = this.gravitationalAccelerationAt(rHalf2, planet);
            const kR3 = v0.add(kV2.multiply(halfDt));

            const rEnd = r0.add(kR3.multiply(scaledDt));
            const kV4 = this.gravitationalAccelerationAt(rEnd, planet);
            const kR4 = v0.add(kV3.multiply(scaledDt));

            planet.rk4PositionUpdate(kR1, kR2, kR3, kR4, scaledDt);
            planet.rk4VelocityUpdate(kV1, kV2, kV3, kV4, scaledDt);
        }
    };


    Gravity3DSimulation.prototype.applyRadiationToPlanet = function (planet, scaledDt) {
        for (const other of this.planets) {
            if (planet === other) continue;
            if (this.pendingRemove.includes(other)) continue;
            if (this.pendingRemove.includes(planet)) return;
            if (other.state.getType() !== PlanetTypes.STAR) continue;
            const distance = planet.distanceTo(other);
            if (distance > other.state.getRadius()) {
                planet.applyRadiationFrom(other, scaledDt);
            }
        }
    };

    Gravity3DSimulation.prototype.radiateHeatAway = function (planet, scaledDt) {
        if (this.pendingRemove.includes(planet)) return;
        if (planet.state.getType() === PlanetTypes.STAR) return;
        planet.applyRadiationAway(scaledDt);
    };
})();
