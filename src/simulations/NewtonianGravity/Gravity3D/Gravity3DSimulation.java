package simulations.NewtonianGravity.Gravity3D;

import simulations.BaseSimulation;
import simulations.NewtonianGravity.Planet;
import simulations.NewtonianGravity.PointMass;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import simulations.NewtonianGravity.Vector;

/**
 * 3D Gravity Simulation - Multiple planets interacting through gravitational forces in 3D space.
 * Uses Graphics2D for rendering (no native dependencies).
 */
public class Gravity3DSimulation extends BaseSimulation {
    /** List of all planets/point masses in the simulation */
    private List<Planet> planets = new ArrayList<>();
    
    /** GLOBAL VARIABLES OF SIMULATION */
    private double gravitationalConstant = 6000.0;
    private boolean bounce = false;
    private boolean useRK4 = false;
    private double coefficientOfRestitution = 1.0;
    private double timeFactor = 1.0;
    
    /** Animation timer - calls update() repeatedly */
    private Timer animationTimer;
    
    /** Time step for physics calculations (in seconds) */
    private static final double DELTA_TIME = 1.0 / 60.0; // 60 FPS
    
    /** Graphics2D panel for 3D rendering */
    private Graphics3DPanel graphicsPanel;
    
    /** Camera for 3D view */
    private Camera camera;
    
    /** Pause state */
    private boolean isPaused = false;
    
    /** Control panel for adding objects */
    private Gravity3DControlPanel controlPanel;
    
    @Override
    public void initialize() {
        System.out.println("[Gravity3D] Initializing simulation...");
        setTitle("3D Gravity Simulation (Graphics2D)");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        planets = new ArrayList<>();
        
        // TODO: Initialize 3D control panel
        // controlPanel = new Gravity3DControlPanel(...);
        
        // Create Graphics2D panel (no native dependencies!)
        System.out.println("[Gravity3D] Creating Graphics3DPanel...");
        graphicsPanel = new Graphics3DPanel();
        
        // Create camera
        System.out.println("[Gravity3D] Creating camera...");
        camera = new Camera();
        camera.setPosition(0, 0, 5); // Position camera at (0, 0, 5) looking at origin
        graphicsPanel.setCamera(camera);
        
        // Initialize sphere mesh
        System.out.println("[Gravity3D] Initializing sphere mesh...");
        SphereRenderer.initializeMesh(32, 16);
        System.out.println("[Gravity3D] Sphere mesh initialized successfully");
        
        // Add render callback - Graphics2D will be provided by paintComponent
        graphicsPanel.addRenderCallback(g2d -> {
            if (camera != null) {
                renderToGraphics(g2d);
            }
        });
        
        setLayout(new BorderLayout());
        // add(controlPanel, BorderLayout.EAST);
        add(graphicsPanel, BorderLayout.CENTER);
        
        setVisible(true);
        System.out.println("[Gravity3D] Window is visible");
    }
    
    @Override
    public void render() {
        // Trigger repaint which will call renderToGraphics
        if (graphicsPanel != null) {
            graphicsPanel.render();
        }
    }
    
    /**
     * Renders the 3D scene to Graphics2D.
     */
    private void renderToGraphics(Graphics2D g2d) {
        if (camera == null || graphicsPanel == null) {
            return;
        }
        
        try {
            // Update camera viewport
            camera.setViewport(graphicsPanel.getGLWidth(), graphicsPanel.getGLHeight());
            
            Matrix4f viewMatrix = camera.getViewMatrix();
            Matrix4f projMatrix = camera.getProjectionMatrix();
            
            // Render a test sphere at origin
            Matrix4f modelMatrix = new Matrix4f().translate(0, 0, 0).scale(1.0f);
            SphereRenderer.render(g2d, modelMatrix, viewMatrix, projMatrix, 
                                 new Vector3f(1.0f, 0.0f, 0.0f),
                                 graphicsPanel.getGLWidth(), graphicsPanel.getGLHeight());
            
            // TODO: Render all planets from the planets list
        } catch (Exception e) {
            System.err.println("[Gravity3D] ERROR in render: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void update(double deltaTime) {
        // Don't update physics if paused
        if (isPaused) {
            return;
        }
        
        List<Planet> toAdd = new ArrayList<>();
        List<Planet> toRemove = new ArrayList<>();

        for (Planet planet : planets) {
            if (toRemove.contains(planet)) continue;
            
            // Skip PointMass objects - they don't move or need force calculations
            if (planet instanceof PointMass) continue;
            
            // Initialize total force components
            Vector totalForce = new Vector(new double[]{0.0, 0.0, 0.0});

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

                // Compute gravitational force
                Vector forceVec = planet.gravitationalForceFrom(other, gravitationalConstant);
                totalForce.add(forceVec);
            }

            // Skip velocity update if planet is set to be removed
            if (toRemove.contains(planet)) continue;

            // Newton's second law: F = ma → a = F/m
            Vector acceleration = totalForce.divide(planet.getMass());

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
    
    
    @Override
    public void start() {
        System.out.println("[Gravity3D] Starting simulation...");
        initialize();
        
        System.out.println("[Gravity3D] Creating animation timer...");
        animationTimer = new Timer(16, e -> {
            update(DELTA_TIME);
            render(); // This triggers repaint
        });
        
        animationTimer.start();
        System.out.println("[Gravity3D] Animation timer started");
    }
    
    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (graphicsPanel != null) {
            graphicsPanel.cleanup();
        }
        SphereRenderer.cleanupStatic();
        dispose();
    }
}

