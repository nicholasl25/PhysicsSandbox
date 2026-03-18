/**
 * Gravity3D Web Simulation - Physics
 * Gravity, radiation, collisions/merge. Extends Gravity3DSimulation prototype.
 */

(function () {
    'use strict';

    /** For one planet: sum gravity from others, handle collision/merge, then F=ma and update velocity. */
    Gravity3DSimulation.prototype.applyForcesToPlanet = function (planet, toRemove, toAdd, scaledDt) {
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
        const state = planet.getState();
        const acceleration = totalForce.divide(state.getMass()); // kg
        planet.updateVelocity(acceleration, scaledDt);
    };

    Gravity3DSimulation.prototype.applyRadiationToPlanet = function (planet, toRemove, toAdd, scaledDt) {
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
    };

    Gravity3DSimulation.prototype.radiateHeatAway = function (planet, toRemove, scaledDt) {
        if (toRemove.includes(planet)) return;
        if (planet.state.getType() === PlanetTypes.STAR) return;
        planet.applyRadiationAway(scaledDt);
    };
})();
