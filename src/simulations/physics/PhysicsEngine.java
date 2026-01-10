package simulations.physics;

import simulations.NewtonianGravity.Planet;
import simulations.NewtonianGravity.PointMass;
import simulations.NewtonianGravity.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PhysicsEngine - Handles all physics calculations for the gravity simulation.
 * Runs independently of rendering, making it suitable for web app architecture.
 */
public class PhysicsEngine {
    
    /** List of all planets/point masses in the simulation */
    private List<Planet> planets = new CopyOnWriteArrayList<>();
    
    /** Simulation settings */
    private double gravitationalConstant = 6000.0;
    private boolean bounce = false;
    // Note: useRK4 is kept for future RK4 integration support
    @SuppressWarnings("unused")
    private boolean useRK4 = false;
    private double coefficientOfRestitution = 1.0;
    private double timeFactor = 1.0;
    
    /** Time step for physics calculations (in seconds) */
    private static final double DELTA_TIME = 1.0 / 60.0; // 60 FPS
    
    /** Dimension of simulation (2 or 3) */
    private int dimension;
    
    /** Pause state */
    private volatile boolean isPaused = false;
    
    /** Running state */
    private volatile boolean running = false;
    
    /** Physics thread */
    private Thread physicsThread;
    
    /**
     * Creates a new physics engine for the specified dimension.
     * @param dimension 2 for 2D, 3 for 3D
     */
    public PhysicsEngine(int dimension) {
        if (dimension != 2 && dimension != 3) {
            throw new IllegalArgumentException("Dimension must be 2 or 3");
        }
        this.dimension = dimension;
    }
    
    /**
     * Starts the physics simulation loop in a separate thread.
     */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        isPaused = false;
        
        physicsThread = new Thread(() -> {
            while (running) {
                if (!isPaused) {
                    update(DELTA_TIME);
                }
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "PhysicsEngine-Thread");
        physicsThread.start();
    }
    
    /**
     * Stops the physics simulation.
     */
    public void stop() {
        running = false;
        if (physicsThread != null) {
            try {
                physicsThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Updates the physics simulation by one time step.
     * This is the core physics loop that calculates forces and updates positions.
     */
    public void update(double deltaTime) {
        List<Planet> toAdd = new ArrayList<>();
        List<Planet> toRemove = new ArrayList<>();

        for (Planet planet : planets) {
            if (toRemove.contains(planet)) continue;
            
            // Skip PointMass objects - they don't move or need force calculations
            if (planet instanceof PointMass) continue;
            
            // Initialize total force vector (works for both 2D and 3D)
            Vector totalForce = new Vector(dimension);

            for (Planet other : planets) {
                if (planet == other) continue;
                if (toRemove.contains(other)) continue;

                // Check for collisions
                if (planet.collidesWith(other)) {
                    if (bounce) {
                        if (other instanceof PointMass) {
                            planet.bouncePointMass(coefficientOfRestitution);
                        } else {
                            planet.bouncePlanet(coefficientOfRestitution, other);
                        }
                    } else {
                        // Handle merge - PointMass always wins
                        if (other instanceof PointMass) {
                            PointMass merged = ((PointMass) other).merge(planet);
                            toAdd.add(merged);
                            toRemove.add(planet);
                            toRemove.add(other);
                        } else {
                            Planet merged = planet.merge(other);
                            toAdd.add(merged);
                            toRemove.add(planet);
                            toRemove.add(other);
                        }
                    }
                    break; // stop computing further for this planet
                }

                // Compute gravitational force (returns a Vector, works for 2D and 3D)
                Vector forceVec = planet.gravitationalForceFrom(other, gravitationalConstant);
                totalForce = totalForce.add(forceVec);
            }

            // Skip velocity update if planet is set to be removed
            if (toRemove.contains(planet)) continue;

            // Newton's second law: F = ma → a = F/m
            Vector acceleration = totalForce.multiply(1.0 / planet.getMass());
            planet.updateVelocity(acceleration, deltaTime * timeFactor);
        }

        // Apply removals and additions safely after iteration
        planets.removeAll(toRemove);
        planets.addAll(toAdd);
        
        // Update positions based on velocities (after all velocities are updated)
        for (Planet planet : planets) {
            planet.updatePosition(deltaTime, timeFactor);
        }
    }
    
    /**
     * Gets a copy of the current planets list.
     */
    public List<Planet> getPlanets() {
        return new ArrayList<>(planets);
    }
    
    /**
     * Adds a planet to the simulation.
     */
    public void addPlanet(Planet planet) {
        if (planet.getPosition().dimensions() != dimension) {
            throw new IllegalArgumentException("Planet dimension must match engine dimension");
        }
        planets.add(planet);
    }
    
    /**
     * Removes a planet from the simulation.
     */
    public void removePlanet(Planet planet) {
        planets.remove(planet);
    }
    
    /**
     * Clears all planets from the simulation.
     */
    public void clearPlanets() {
        planets.clear();
    }
    
    // Getters and setters for simulation settings
    public double getGravitationalConstant() {
        return gravitationalConstant;
    }
    
    public void setGravitationalConstant(double gravitationalConstant) {
        this.gravitationalConstant = gravitationalConstant;
    }
    
    public boolean isBounce() {
        return bounce;
    }
    
    public void setBounce(boolean bounce) {
        this.bounce = bounce;
    }
    
    public double getCoefficientOfRestitution() {
        return coefficientOfRestitution;
    }
    
    public void setCoefficientOfRestitution(double coefficientOfRestitution) {
        this.coefficientOfRestitution = coefficientOfRestitution;
    }
    
    public double getTimeFactor() {
        return timeFactor;
    }
    
    public void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getDimension() {
        return dimension;
    }
}
