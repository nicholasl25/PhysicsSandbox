package com.physics.simulations.gravity;

import com.physics.simulations.BaseSimulation;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.awt.BasicStroke;

/**
 * Gravity Simulation - Multiple planets interacting through gravitational forces
 */
public class GravitySimulation extends BaseSimulation {
    /** List of all planets/point masses in the simulation */
    private List<Planet> planets = new ArrayList<>();
     private List<PointMass> masses = new ArrayList<>();
    
    /** GLOBAL VARAIBLES OF SIMULATION */
    private double gravitationalConstant = 6000.0;
    private boolean bounce = true;
    private double coefficientOfRestitution = 1.0;
    
    /** Animation timer - calls update() repeatedly */
    private Timer animationTimer;
    
    /** Time step for physics calculations (in seconds) */
    private static final double DELTA_TIME = 1.0 / 60.0; // 60 FPS
    
    /** Drawing panel - custom component for rendering */
    private DrawingPanel drawingPanel;

    /** Pan offsets (in screen coordinates) */
    private double panLevelX = 0.0;
    private double panLevelY = 0.0;

    /** Mouse drag tracking */
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean isDragging = false;
    private boolean hasDragged = false; // Track if mouse moved during press/release
    private int mousePressX = 0;
    private int mousePressY = 0;
    
    /** Pause state */
    private boolean isPaused = false;
    
    /** Control panel for adding objects */
    private ControlPanel controlPanel;
    private double clickedWorldX, clickedWorldY;

    private Planet clickedPlanet = null;
    
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
        
        // Create control panel
        controlPanel = new ControlPanel(
            this::addPlanetFromFields,
            this::addStationaryMassFromFields,
            this::clearSimulation,
            this::updateGravity
        );
        
        // Initialize clicked position to center
        clickedWorldX = 500.0;
        clickedWorldY = 400.0;
        
        // Create a custom drawing panel to handle rendering
        // We'll override its paintComponent() method to draw our planets
        drawingPanel = new DrawingPanel();
        setupMouseListeners();
        
        // Add components to main window
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.EAST);
        add(drawingPanel, BorderLayout.CENTER);
        
        // Set up spacebar key binding at root pane level to prevent buttons from intercepting it
        setupKeyBindings();
        
        // Make sure the panel can receive focus for keyboard events
        drawingPanel.setFocusable(true);
        drawingPanel.requestFocus();
        
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
        PointMass mass = new PointMass(500, 500, 500);
        masses.add(mass);
    }
    
    /**
     * Sets up mouse listeners for pan
     */
    private void setupMouseListeners() {
        // Mouse listeners for drag-to-pan
        drawingPanel.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Left mouse button
                    isDragging = true;
                    hasDragged = false;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    mousePressX = e.getX();
                    mousePressY = e.getY();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isDragging = false;
                    
                    // If mouse didn't move much (or at all), treat it as a click
                    // Update click position only if we didn't drag significantly
                    int dragDistance = (int) Math.sqrt(
                        Math.pow(e.getX() - mousePressX, 2) + 
                        Math.pow(e.getY() - mousePressY, 2)
                    );
                    
                    // If drag distance is small (less than 5 pixels), treat as click
                    if (!hasDragged || dragDistance < 5) {
                        // Convert screen coordinates to world coordinates
                        clickedWorldX = e.getX() - panLevelX;
                        clickedWorldY = e.getY() - panLevelY;
                        
                        // Check if the clicked position is on a planet
                        for (Planet planet : planets) {
                            if (planet.containsPoint(clickedWorldX, clickedWorldY)) {
                                clickedPlanet = planet;
                                planet.clicked();
                                break;
                            }
                        }
                        
                        // Repaint to show the red X marker
                        drawingPanel.repaint();
                    }
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // This can be unreliable, so we handle clicks in mouseReleased instead
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {}
            
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        
        // Mouse motion listener for drag
        drawingPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    hasDragged = true; // Mark that dragging occurred
                    int currentX = e.getX();
                    int currentY = e.getY();
                    
                    // Calculate delta (difference in mouse position)
                    int deltaX = currentX - lastMouseX;
                    int deltaY = currentY - lastMouseY;
                    
                    // Update pan - move the view in the direction of the drag
                    panLevelX += deltaX;
                    panLevelY += deltaY;
                    
                    // Update last mouse position
                    lastMouseX = currentX;
                    lastMouseY = currentY;
                    
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
    }
    
    /**
     * Adds a planet using values from the control panel
     */
    private void addPlanetFromFields() {
        ControlPanel.PlanetData data = controlPanel.getPlanetData();
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Planet newPlanet = new Planet(data.mass, data.radius, clickedWorldX, clickedWorldY, 
                                     data.vx, data.vy, data.color);
        planets.add(newPlanet);
        drawingPanel.repaint();
    }
    
    /**
     * Adds a stationary mass using values from the control panel
     */
    private void addStationaryMassFromFields() {
        ControlPanel.StationaryMassData data = controlPanel.getStationaryMassData();
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        PointMass newMass = new PointMass(data.mass, clickedWorldX, clickedWorldY, 
                                         data.radius, data.color);
        masses.add(newMass);
        drawingPanel.repaint();
    }
    
    /**
     * Updates the gravitational constant from the slider
     */
    private void updateGravity(Double newGravity) {
        gravitationalConstant = newGravity;
    }
    
    /**
     * Clears all planets and point masses from the simulation
     */
    private void clearSimulation() {
        planets.clear();
        masses.clear();
        drawingPanel.repaint();
    }
    
    /**
     * Sets up key bindings for pause/resume.
     * Uses root pane bindings to ensure spacebar always works, even when buttons have focus.
     */
    private void setupKeyBindings() {
        // Get the root pane for global key bindings
        JRootPane rootPane = getRootPane();
        
        // Create the action for pause/resume
        AbstractAction pauseResumeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPaused = !isPaused;
                repaint();
            }
        };
        
        // Bind spacebar at the root pane level with WHEN_IN_FOCUSED_WINDOW
        // This ensures it works regardless of which component has focus
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        inputMap.put(spaceKey, "pauseResume");
        actionMap.put("pauseResume", pauseResumeAction);
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
        // Don't update physics if paused
        if (isPaused) {
            return;
        }
        
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
                    if (bounce = true) {
                        planet.bouncePlanet(coefficientOfRestitution, other);
                    }
                    else {
                        Planet merged = planet.merge(other);
                        toAdd.add(merged);       // new merged planet to add later
                        toRemove.add(planet);    // both old ones should be removed
                        toRemove.add(other); }
                    break;                   // stop computing further for this planet
                }

                // Compute gravitational force
                double[] force = planet.gravitationalForceFrom(other, gravitationalConstant);
                totalForceX += force[0];
                totalForceY += force[1];
            }

            for (PointMass mass : masses) {
                if (mass.collidesWith(planet)) {
                    if (bounce == true) {
                        planet.bouncePointMass(coefficientOfRestitution);
                    }
                    else {
                        mass.merge(planet);
                        toRemove.add(planet); }
                    break;                         // stop computing further for this planet
                }

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
        
        // Update positions based on velocities (after all velocities are updated)
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

            // Save the original transform for text drawing
            AffineTransform originalTransform = g2d.getTransform();
            
            // Apply transformations: pan only
            g2d.translate(panLevelX, panLevelY);
            
            // Draw grid background for position reference
            drawGrid(g2d);
            
            // Draw all planets
            if (planets != null) {
                for (Planet planet : planets) {
                    planet.draw(g2d);
                }
            }
            
            // Draw all point masses
            if (masses != null) {
                for (PointMass mass : masses) {
                    mass.draw(g2d);
                }
            }

            if (clickedPlanet != null) {
                Planet selectedPlanet = clickedPlanet;
                g2d.setTransform(originalTransform);

                // Draw info box
                int infoX = 10;
                int infoY = 100;
                int boxWidth = 250;
                int boxHeight = 150;

                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(infoX, infoY, boxWidth, boxHeight);
                
                // Border
                g2d.setColor(Color.YELLOW);
                g2d.drawRect(infoX, infoY, boxWidth, boxHeight);
                
                // Planet info text
                g2d.setColor(Color.WHITE);
                int textY = infoY + 20;
                g2d.drawString("=== SELECTED PLANET ===", infoX + 10, textY);
                textY += 20;
                g2d.drawString(String.format("Mass: %.2f", selectedPlanet.mass), infoX + 10, textY);
                textY += 20;
                g2d.drawString(String.format("Radius: %.2f", selectedPlanet.radius), infoX + 10, textY);
                textY += 20;
                g2d.drawString(String.format("Position: (%.1f, %.1f)", selectedPlanet.x, selectedPlanet.y), 
                            infoX + 10, textY);
                textY += 20;
                g2d.drawString(String.format("Velocity: (%.2f, %.2f)", selectedPlanet.vx, selectedPlanet.vy), 
                            infoX + 10, textY);
                textY += 20;
                
                // Speed calculation
                double speed = Math.sqrt(selectedPlanet.vx * selectedPlanet.vx + 
                                        selectedPlanet.vy * selectedPlanet.vy);
                g2d.drawString(String.format("Speed: %.2f", speed), infoX + 10, textY);
            }
                        
            // Draw red X marker at last click position
            drawClickMarker(g2d);
            
            // Restore original transform for text (so it's not zoomed/panned)
            g2d.setTransform(originalTransform);
            
            // Draw info text (always at same screen position, not affected by zoom/pan)
            g2d.setColor(Color.WHITE);
            g2d.drawString("Planets: " + (planets != null ? planets.size() : 0), 10, 20);
            g2d.drawString("Point Masses: " + (masses != null ? masses.size() : 0), 10, 35);
            g2d.drawString("G = " + gravitationalConstant, 10, 50);
            if (isPaused) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("PAUSED - Press SPACE to resume", 10, 65);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawString("Controls: Click to set position, then use panel on right. Drag = pan, SPACE = pause/resume", 10, getHeight() - 10);
        }

        /**
         * Draws a grid background to provide visual position reference.
         * The grid pans with the view.
         */
        private void drawGrid(Graphics2D g2d) {
            // Grid spacing (in world coordinates)
            int gridSpacing = 100;
            
            // Draw a large grid covering a wide area
            // Java will automatically clip lines outside the visible area
            int gridSize = 10000; // Large area covered by grid
            int startX = -gridSize;
            int endX = gridSize;
            int startY = -gridSize;
            int endY = gridSize;
            
            // Set grid color and stroke
            g2d.setColor(new Color(40, 40, 40)); // Dark gray
            g2d.setStroke(new BasicStroke(1.0f));
            
            // Draw vertical lines
            for (int x = startX; x <= endX; x += gridSpacing) {
                g2d.drawLine(x, startY, x, endY);
            }
            
            // Draw horizontal lines
            for (int y = startY; y <= endY; y += gridSpacing) {
                g2d.drawLine(startX, y, endX, y);
            }
        }
        
        /**
         * Draws a red X marker at the last click position
         */
        private void drawClickMarker(Graphics2D g2d) {
            // Size of the X marker
            int markerSize = 15;
            int halfSize = markerSize / 2;
            
            // Set color and stroke for the X
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2.0f));
            
            // Draw the X: two diagonal lines
            int x = (int) clickedWorldX;
            int y = (int) clickedWorldY;
            
            // Draw diagonal lines forming an X
            g2d.drawLine(x - halfSize, y - halfSize, x + halfSize, y + halfSize);
            g2d.drawLine(x - halfSize, y + halfSize, x + halfSize, y - halfSize);
        }
    }
}
