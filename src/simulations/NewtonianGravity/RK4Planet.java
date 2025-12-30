package simulations.NewtonianGravity;

import java.awt.Color;

/**
 * RK4Planet - A Planet that uses RK4 (Runge-Kutta 4th order) integration
 * instead of Euler integration for better numerical accuracy.
 */
public class RK4Planet extends Planet {
    
    // RK4 integration fields
    private Vector k1vel, k1pos;
    private Vector k2vel, k2pos;
    private Vector k3vel, k3pos;
    private Vector k4vel, k4pos;
    private Vector lastAccel;
    
    /**
     * Constructor with Vector position and velocity (supports 2D and 3D).
     */
    public RK4Planet(int dimension, double mass, double radius, Vector pos, Vector vel,
    double angularVelocity, double temperature, Color color, String texturePath, String name) {
        super(dimension, mass, radius, pos, vel, angularVelocity, temperature, color, texturePath, name);
        
        // Initialize RK4 fields with same dimensions
        this.k1vel = new Vector(new double[dimension]);
        this.k1pos = new Vector(new double[dimension]);
        this.k2vel = new Vector(new double[dimension]);
        this.k2pos = new Vector(new double[dimension]);
        this.k3vel = new Vector(new double[dimension]);
        this.k3pos = new Vector(new double[dimension]);
        this.k4vel = new Vector(new double[dimension]);
        this.k4pos = new Vector(new double[dimension]);
        this.lastAccel = new Vector(new double[dimension]);
    }
    
    /**
     * Constructor for 2D compatibility (creates 2D vectors from x, y, vx, vy).
     */
    public RK4Planet(double mass, double radius, double x, double y, double vx, double vy,
    double angularVelocity, double temperature, Color color, String texturePath, String name) {
        this(2, mass, radius, new Vector(new double[]{x, y}), new Vector(new double[]{vx, vy}), 
             angularVelocity, temperature, color, texturePath, name);
    }
    
    /**
     * Overrides updateVelocity to use RK4 integration.
     */
    @Override
    public void updateVelocity(Vector accel, double deltaTime) {
        // Store acceleration for position RK4
        lastAccel = accel.clone();
        
        // k1: acceleration at current state
        k1vel = accel.multiply(deltaTime);
        
        // k2: acceleration at midpoint (estimate using k1)
        // Assume acceleration changes linearly: a(t+dt/2) ≈ a(t)
        k2vel = accel.multiply(deltaTime);
        
        // k3: acceleration at midpoint using k2 (same estimate)
        k3vel = accel.multiply(deltaTime);
        
        // k4: acceleration at end using k3
        k4vel = accel.multiply(deltaTime);
        
        // Weighted average: v += (k1 + 2*k2 + 2*k3 + k4) / 6
        Vector weightedAvg = k1vel.add(k2vel.multiply(2.0))
                                   .add(k3vel.multiply(2.0))
                                   .add(k4vel);
        weightedAvg = weightedAvg.multiply(1.0 / 6.0);
        vel = vel.add(weightedAvg);
    }
    
    /**
     * Overrides updatePosition to use RK4 integration.
     */
    @Override
    public void updatePosition(double deltaTime, double timeFactor) {
        double dt = deltaTime * timeFactor;
        
        // k1: velocity at current state
        k1pos = vel.multiply(dt);
        
        // k2: velocity at midpoint using k1
        // v(t + dt/2) ≈ v(t) + a(t) * dt/2
        Vector velMid1 = vel.add(lastAccel.multiply(dt * 0.5));
        k2pos = velMid1.multiply(dt);
        
        // k3: velocity at midpoint using k2
        Vector velMid2 = vel.add(lastAccel.multiply(dt * 0.5));
        k3pos = velMid2.multiply(dt);
        
        // k4: velocity at end using k3
        // v(t + dt) ≈ v(t) + a(t) * dt
        Vector velEnd = vel.add(lastAccel.multiply(dt));
        k4pos = velEnd.multiply(dt);
        
        // Weighted average: pos += (k1 + 2*k2 + 2*k3 + k4) / 6
        Vector weightedAvg = k1pos.add(k2pos.multiply(2.0))
                                   .add(k3pos.multiply(2.0))
                                   .add(k4pos);
        weightedAvg = weightedAvg.multiply(1.0 / 6.0);
        pos = pos.add(weightedAvg);
        
        // Update rotation angle (same as parent)
        rotationAngle += angularVelocity * timeFactor * deltaTime;
        rotationAngle %= (Math.PI * 2);
        if (rotationAngle < 0) {
            rotationAngle += Math.PI * 2;
        }
    }
}

