package com.physics.simulations.gravity;

import com.physics.simulations.BaseSimulation;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Gravity Simulation - Multiple planets interacting through gravitational forces
 */
public class GravitySimulation extends BaseSimulation {
    /** List of all planets/point masses in the simulation */
    private List<Planet> planets = new ArrayList<>();
     private List<PointMass> masses = new ArrayList<>();
    
    /** Gravitational constant (scaled for visualization - not real-world value) */
    private double gravitationalConstant = 6000.0;
    
    /** Animation timer - calls update() repeatedly */
    private Timer animationTimer;
    
    /** Time step for physics calculations (in seconds) */
    private static final double DELTA_TIME = 1.0 / 60.0; // 60 FPS
    
    /** Drawing panel - custom component for rendering */
    private DrawingPanel drawingPanel;
    
    /**
     * Sets up the simulation window and creates initial planets.
     */
    @Override
    public void initialize() {
        // Set window properties
        setTitle("Gravity Simulation");
        setSize(1000, 800);
        setLocationRelativeTo(null); // Center window on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Initialize the list of planets
        planets = new ArrayList<>();
        masses = new ArrayList<>();
        
        // Create a custom drawing panel to handle rendering
        // We'll override its paintComponent() method to draw our planets
        drawingPanel = new DrawingPanel();
        add(drawingPanel);
        
        // Create initial planets
        setupPlanets();
        setupMasses();
    }
    
    /**
     * Creates large mass at the center of the screen and a smaller mass orbiting it.
     */
    private void setupPlanets() {
        Planet sun = new Planet(
            1000.0,                   
            20.0,                      
            500.0, 400.0,        
            0.0, 0.0,              
            Color.YELLOW
        );


        Planet planet1 = new Planet(
            50.0,                     
            10.0,                 
            700.0, 400.0,
            0.0, -80.0,
            Color.BLUE
        );
        
        planets.add(sun);
        planets.add(planet1);
    }

    private void setupMasses() {
        PointMass mass = new PointMass(500, 500, 600);
        masses.add(mass);
    }
    
    
    // ============================================
    // UPDATE METHOD
    // ============================================
    /**
     * Updates the physics simulation by one time at a time
     * 
     * @param deltaTime Time elapsed since last update (in seconds)
     */
    @Override
    public void update(double deltaTime) {
        
        List<Planet> toAdd = new ArrayList<>();
        List<Planet> toRemove = new ArrayList<>();

        for (Planet planet : planets) {
            // Initialize total force components
            double totalForceX = 0.0;
            double totalForceY = 0.0;

            for (Planet other : planets) {
                if (planet == other) continue;

                // Check for collisions
                if (planet.collidesWith(other)) {
                    Planet merged = planet.handleCollision(other);
                    toAdd.add(merged);       // new merged planet to add later
                    toRemove.add(planet);    // both old ones should be removed
                    toRemove.add(other);
                    break;                   // stop computing further for this planet
                }

                // Compute gravitational force
                double[] force = planet.gravitationalForceFrom(other, gravitationalConstant);
                totalForceX += force[0];
                totalForceY += force[1];
            }

            for (PointMass mass : masses) {
                double[] force = mass.gravitationalForceFrom(planet, gravitationalConstant);
                // Equal and opposite forces but pointmasses don't move
                totalForceX -= force[0];
                totalForceY -= force[1];
            }

            // Skip velocity update if planet is set to be removed
            if (toRemove.contains(planet)) continue;

            // Newton's second law: F = ma → a = F/m
            double accelerationX = totalForceX / planet.mass;
            double accelerationY = totalForceY / planet.mass;

            planet.updateVelocity(accelerationX, accelerationY, deltaTime);
        }

        // Apply removals and additions safely after iteration
        planets.removeAll(toRemove);
        planets.addAll(toAdd);
        
        // Step 4: Update positions based on velocities
        // Do this after all velocities are updated (ensures consistent physics)
        for (Planet planet : planets) {
            planet.updatePosition(deltaTime);
        }
    }
    
    
    @Override
    public void render() {
        repaint();
    }
    
    
    @Override
    public void start() {
        initialize();
    
        // 1000ms / 60 FPS ≈ 16.67ms per frame
        animationTimer = new Timer(16, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update(DELTA_TIME);
                render();
            }
        });
        
        setVisible(true);
        
        // Start the animation timer
        animationTimer.start();
    }
    
    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        dispose();
    }
    
    

    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Important! Clears previous frame
            
            // Cast Graphics to Graphics2D for better drawing capabilities
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable anti-aliasing - makes edges smoother (less pixelated)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw dark space background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw all planets
            if (planets != null) {
                for (Planet planet : planets) {
                    planet.draw(g2d);
                }
            }
            
            // Optional: Draw some info text
            g2d.setColor(Color.WHITE);
            g2d.drawString("Planets: " + (planets != null ? planets.size() : 0), 10, 20);
            g2d.drawString("G = " + gravitationalConstant, 10, 35);
        }
    }
}
