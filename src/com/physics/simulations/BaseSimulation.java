package com.physics.simulations;

import javax.swing.JFrame;

/**
 * Base class for all physics simulations.
 * 
 * This is an ABSTRACT class - notice the "abstract" keyword.
 * It provides a template that all simulations must follow,
 * but doesn't provide the implementation itself.
 * 
 * IMPORTANT: Each simulation is completely independent/disjoint.
 * Simulations are organized by domain (astronomy, fluids, mechanics, etc.)
 * and don't share implementation code between them.
 * 
 * Learning Concepts:
 * - Abstract classes: Can't be instantiated directly, but define structure
 * - Inheritance: Your simulations will "extend" this class
 * - Polymorphism: You can treat all simulations the same way through this interface
 * 
 * TODO: Implement the methods in your simulation classes!
 */
public abstract class BaseSimulation extends JFrame {
    
    /**
     * Called when the simulation starts.
     * Use this to initialize physics variables, set up the display, etc.
     */
    public abstract void initialize();
    
    /**
     * Updates the simulation by one time step.
     * This is where you'll implement your physics calculations.
     * 
     * @param deltaTime The time elapsed since last update (in seconds)
     */
    public abstract void update(double deltaTime);
    
    /**
     * Renders/redraws the simulation on screen.
     * Use this to draw particles, trajectories, etc.
     */
    public abstract void render();
    
    /**
     * Starts the simulation loop.
     * This method handles the timing and calls update() and render() repeatedly.
     */
    public abstract void start();
    
    /**
     * Stops the simulation and cleans up resources.
     */
    public abstract void stop();
}

