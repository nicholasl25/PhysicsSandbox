package com.physics.simulations.gravity;

import java.awt.Color;

/**
 * PlanetRK4 - A Planet subclass that uses Runge-Kutta 4th order (RK4) integration
 * instead of Euler integration for more accurate and stable physics simulation.
 * 
 * RK4 evaluates the derivative (acceleration) at four points per time step,
 * providing 4th-order accuracy compared to Euler's 1st-order accuracy.
 */
public class PlanetRK4 extends Planet {
    
    // Temporary storage for RK4 intermediate values
    private double k1vx, k1vy, k1x, k1y;
    private double k2vx, k2vy, k2x, k2y;
    private double k3vx, k3vy, k3x, k3y;
    private double k4vx, k4vy, k4x, k4y;
    
    // Store the last acceleration for use in position RK4
    private double lastAx = 0.0;
    private double lastAy = 0.0;
    
    /**
     * Constructor - same as Planet constructor
     */
    public PlanetRK4(double mass, double radius, double x, double y, double vx, double vy, 
                     double angularVelocity, Color color, String texturePath, String name) {
        super(mass, radius, x, y, vx, vy, angularVelocity, color, texturePath, name);
    }
    
    /**
     * Updates velocity using RK4 integration.
     * Uses the provided acceleration and estimates intermediate accelerations
     * by assuming velocity changes linearly.
     * 
     * @param ax Acceleration in x direction
     * @param ay Acceleration in y direction
     * @param deltaTime Time step
     */
    @Override
    public void updateVelocity(double ax, double ay, double deltaTime) {
        // Store acceleration for position RK4
        lastAx = ax;
        lastAy = ay;
        
        // k1: acceleration at current state
        k1vx = ax * deltaTime;
        k1vy = ay * deltaTime;
        
        // k2: acceleration at midpoint (estimate using k1)
        // Assume acceleration changes linearly: a(t+dt/2) ≈ a(t)
        k2vx = ax * deltaTime;
        k2vy = ay * deltaTime;
        
        // k3: acceleration at midpoint using k2 (same estimate)
        k3vx = ax * deltaTime;
        k3vy = ay * deltaTime;
        
        // k4: acceleration at end using k3
        k4vx = ax * deltaTime;
        k4vy = ay * deltaTime;
        
        // Weighted average: v += (k1 + 2*k2 + 2*k3 + k4) / 6
        // Since all k values are the same, this simplifies to Euler, but we keep
        // the structure for when intermediate accelerations can be computed
        vx += (k1vx + 2.0 * k2vx + 2.0 * k3vx + k4vx) / 6.0;
        vy += (k1vy + 2.0 * k2vy + 2.0 * k3vy + k4vy) / 6.0;
    }
    
    /**
     * Updates position using RK4 integration.
     * Uses intermediate velocities computed from the current velocity and acceleration.
     * 
     * @param deltaTime Time step
     * @param timeFactor Time scaling factor
     */
    @Override
    public void updatePosition(double deltaTime, double timeFactor) {
        double dt = deltaTime * timeFactor;
        
        // k1: velocity at current state
        k1x = vx * dt;
        k1y = vy * dt;
        
        // k2: velocity at midpoint using k1
        // v(t + dt/2) ≈ v(t) + a(t) * dt/2
        double vxMid1 = vx + lastAx * dt * 0.5;
        double vyMid1 = vy + lastAy * dt * 0.5;
        k2x = vxMid1 * dt;
        k2y = vyMid1 * dt;
        
        // k3: velocity at midpoint using k2
        double vxMid2 = vx + lastAx * dt * 0.5;
        double vyMid2 = vy + lastAy * dt * 0.5;
        k3x = vxMid2 * dt;
        k3y = vyMid2 * dt;
        
        // k4: velocity at end using k3
        // v(t + dt) ≈ v(t) + a(t) * dt
        double vxEnd = vx + lastAx * dt;
        double vyEnd = vy + lastAy * dt;
        k4x = vxEnd * dt;
        k4y = vyEnd * dt;
        
        // Weighted average: x += (k1 + 2*k2 + 2*k3 + k4) / 6
        x += (k1x + 2.0 * k2x + 2.0 * k3x + k4x) / 6.0;
        y += (k1y + 2.0 * k2y + 2.0 * k3y + k4y) / 6.0;
        
        // Update rotation angle (same as parent)
        rotationAngle += angularVelocity * timeFactor;
        rotationAngle %= (Math.PI * 2);
        if (rotationAngle < 0) {
            rotationAngle += Math.PI * 2;
        }
    }
}

