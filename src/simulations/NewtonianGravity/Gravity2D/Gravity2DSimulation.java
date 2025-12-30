package simulations.NewtonianGravity.Gravity2D;

import simulations.BaseSimulation;
import simulations.NewtonianGravity.Planet;
import simulations.NewtonianGravity.PointMass;
import simulations.NewtonianGravity.RK4Planet;
import simulations.NewtonianGravity.Vector;
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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * 2D Gravity Simulation - Multiple planets interacting through gravitational forces
 */
public class Gravity2DSimulation extends BaseSimulation {
    /** List of all planets/point masses in the simulation */
    private List<Planet> planets = new ArrayList<>();
    
    /** GLOBAL VARAIBLES OF SIMULATION */
    private double gravitationalConstant = 6000.0;
    private boolean bounce = false;
    private boolean useRK4 = false;
    private double coefficientOfRestitution = 1.0;
    private double timeFactor = 1.0;
    
    /** Animation timer - calls update() repeatedly */
    private Timer animationTimer;
    
    /** Time step for physics calculations (in seconds) */
    private static final double DELTA_TIME = 1.0 / 60.0; // 60 FPS
    
    /** Drawing panel - custom component for rendering */
    private DrawingPanel drawingPanel;

    /** Pan offsets (in screen coordinates) */
    private double panLevelX = 0.0;
    private double panLevelY = 0.0;
    
    /** Zoom level (1.0 = normal, 2.0 = 2x zoom, 0.5 = zoomed out) */
    private double zoomLevel = 1.0;
    
    /** Stars background image */
    private BufferedImage starsBackground;

    /** Mouse drag tracking */
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean isDragging = false;
    private boolean hasDragged = false; // Track if mouse moved during press/release
    private int mousePressX = 0;
    private int mousePressY = 0;
    
    /** Pause state */
    private boolean isPaused = false;
    
    /** Sidebar visibility state */
    private boolean sidebarVisible = true;
    
    /** Control panel for adding objects */
    private Gravity2DControlPanel controlPanel;
    private double clickedWorldX, clickedWorldY;

    private Planet clickedPlanet = null;

    private double maxMass = 100000;
    private double maxRadius = 100;
    private double maxVelocity = 1000;
    private int maxObjects = 100;
    private double maxTemperature = 100000;
    private int planetCounter = 1;  // Counter for automatic planet naming
    
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
        
        // Create control panel
        controlPanel = new Gravity2DControlPanel(
            this::addPlanetFromFields,
            this::clearSimulation,
            this::updateGravity,
            this::updateTimeFactor,
            this::updateBounce,
            this::updateRK4
        );
        
        // Initialize clicked position to center
        clickedWorldX = 500.0;
        clickedWorldY = 400.0;
        
        // Load stars background
        loadStarsBackground();
        
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
            0.0, 0.0, 0.02, 5778.0,             
            Color.YELLOW, 
            "resources/textures/Sun.jpg",
            "Sun"
        );


        Planet planet1 = new Planet(
            50.0,                     
            10.0,                 
            700.0, 400.0,
            0.0, -80.0, 0.06, 288.0,
            Color.BLUE,
            "resources/textures/Earth.jpg",
            "Earth"
        );
        
        planets.add(sun);
        planets.add(planet1);
    }

    private void setupMasses() {
        PointMass mass = new PointMass(500, 10, 500, 500, 0.0, 300.0, Color.WHITE, null, null);
        planets.add(mass);
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
                        // Convert screen coordinates to world coordinates using helper method
                        double[] worldCoords = screenToWorld(e.getX(), e.getY());
                        clickedWorldX = worldCoords[0];
                        clickedWorldY = worldCoords[1];
                        
                        // Check if the clicked position is on a planet
                        for (Planet planet : planets) {
                            if (planet.containsPoint(clickedWorldX, clickedWorldY)) {
                                if (clickedPlanet != null) {
                                    clickedPlanet.clicked();
                                }
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
                    
                    // Calculate delta (difference in mouse position) - scale by zoom for proportional movement
                    int deltaX = currentX - lastMouseX;
                    int deltaY = currentY - lastMouseY;
                    
                    // Update pan - move the view in the direction of the drag (proportional to zoom)
                    panLevelX += deltaX / zoomLevel;
                    panLevelY += deltaY / zoomLevel;
                    
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
        Gravity2DControlPanel.PlanetData data = controlPanel.getPlanetData();
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (data.mass > maxMass) {
            JOptionPane.showMessageDialog(this, "Please limit planet masses to " + maxMass + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (data.radius > maxRadius) {
            JOptionPane.showMessageDialog(this, "Please limit planet radii to " + maxRadius + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ((data.vx > maxVelocity) || (data.vy > maxVelocity)) {
            JOptionPane.showMessageDialog(this, "Please limit planet velocities to " + maxVelocity + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (data.temperature > maxTemperature) {
            JOptionPane.showMessageDialog(this, "Please limit planet temperatures to " + maxTemperature + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (planetCounter > maxObjects) {
            JOptionPane.showMessageDialog(this, "Too many objects currently in simulation.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Determine planet name: use provided name or generate "Planet #N" or "PointMass #N"
        String planetName;
        if (data.name != null && !data.name.trim().isEmpty()) {
            planetName = data.name.trim();
        } else {
            if (data.fixedLocation) {
                planetName = "PointMass #" + planetCounter;
            } else {
                planetName = "Planet #" + planetCounter;
            }
            planetCounter++;
        }
        
            Planet newObject;
            if (data.fixedLocation) {
                // Create a PointMass (stationary) - calculate angular velocity from period
                double angularVelocity = data.getAngularVelocity();
                newObject = new PointMass(data.mass, data.radius, clickedWorldX, clickedWorldY, 
                                         angularVelocity, data.temperature, data.color, data.texturePath, planetName);
        } else {
            // Create a Planet or RK4Planet based on integration method
            double angularVelocity = data.getAngularVelocity();
            if (useRK4) {
                newObject = new RK4Planet(data.mass, data.radius, clickedWorldX, clickedWorldY, 
                                          data.vx, data.vy, angularVelocity, data.temperature, data.color, data.texturePath, planetName);
            } else {
                newObject = new Planet(data.mass, data.radius, clickedWorldX, clickedWorldY, 
                                      data.vx, data.vy, angularVelocity, data.temperature, data.color, data.texturePath, planetName);
            }
        }
        
        planets.add(newObject);
        drawingPanel.repaint();
    }
    
    /**
     * Updates the gravitational constant from the slider
     */
    private void updateGravity(Double newGravity) {
        gravitationalConstant = newGravity;
    }
    
    private void updateTimeFactor(Double newTimeFactor) {
        timeFactor = newTimeFactor;
    }
    
    private void updateBounce(Boolean newBounce) {
        bounce = newBounce;
    }
    
    private void updateRK4(Boolean newRK4) {
        useRK4 = newRK4;
    }
    
    /**
     * Loads the stars background image
     */
    private void loadStarsBackground() {
        try {
            File starsFile = new File("resources/textures/Stars.png");
            starsBackground = ImageIO.read(starsFile);
            System.out.println("Loaded stars background: " + starsFile.getPath());
        } catch (IOException e) {
            System.err.println("Failed to load stars background: " + e.getMessage());
            starsBackground = null;
        }
    }
    
    /**
     * Clears all planets and point masses from the simulation
     */
    private void clearSimulation() {
        planets.clear();
        planetCounter = 1;  // Reset counter when simulation is cleared
        drawingPanel.repaint();
    }
    
    /**
     * Converts screen coordinates to world coordinates.
     * Uses AffineTransform to exactly reverse the drawing transformation.
     * 
     * @param screenX Screen X coordinate (relative to drawing panel)
     * @param screenY Screen Y coordinate (relative to drawing panel)
     * @return Array [worldX, worldY]
     */
    private double[] screenToWorld(int screenX, int screenY) {
        // Use the same center calculation as drawing code
        int centerX = drawingPanel.getWidth() / 2;
        int centerY = drawingPanel.getHeight() / 2;
        
        // Build the forward transformation (same as drawing code)
        AffineTransform transform = new AffineTransform();
        transform.translate(centerX, centerY);
        transform.scale(zoomLevel, zoomLevel);
        transform.translate(-centerX, -centerY);
        transform.translate(panLevelX, panLevelY);
        
        // Reverse the transformation
        try {
            AffineTransform inverse = transform.createInverse();
            double[] src = {screenX, screenY};
            double[] dst = new double[2];
            inverse.transform(src, 0, dst, 0, 1);
            return dst;
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            // Should never happen with valid zoom/pan, but return screen coords if it does
            return new double[]{screenX, screenY};
        }
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
        
        // Zoom in action (+ key)
        AbstractAction zoomInAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomLevel *= 2.0;
                drawingPanel.repaint();
            }
        };
        
        // Zoom out action (- key)
        AbstractAction zoomOutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomLevel /= 2.0;
                drawingPanel.repaint();
            }
        };
        
        // Bind + and - keys for zoom
        KeyStroke plusKey = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0);
        KeyStroke minusKey = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0);
        KeyStroke equalsKey = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0); // + without shift
        
        inputMap.put(plusKey, "zoomIn");
        inputMap.put(equalsKey, "zoomIn");
        inputMap.put(minusKey, "zoomOut");
        
        actionMap.put("zoomIn", zoomInAction);
        actionMap.put("zoomOut", zoomOutAction);
        
        // Toggle sidebar action (Tab key)
        AbstractAction toggleSidebarAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSidebar();
            }
        };
        
        KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        inputMap.put(tabKey, "toggleSidebar");
        actionMap.put("toggleSidebar", toggleSidebarAction);
    }
    
    /**
     * Toggles the sidebar (ControlPanel) visibility
     */
    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        if (sidebarVisible) {
            add(controlPanel, BorderLayout.EAST);
        } else {
            remove(controlPanel);
        }
        revalidate();
        repaint();
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
            if (toRemove.contains(planet)) continue;
            
            // Skip PointMass objects - they don't move or need force calculations
            if (planet instanceof PointMass) continue;
            
            // Initialize total force components
            double totalForceX = 0.0;
            double totalForceY = 0.0;

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
                double[] force = forceVec.getData();
                totalForceX += force[0];
                totalForceY += force[1];
            }

            // Skip velocity update if planet is set to be removed
            if (toRemove.contains(planet)) continue;

            // Newton's second law: F = ma → a = F/m
            double accelerationX = totalForceX / planet.getMass();
            double accelerationY = totalForceY / planet.getMass();

            planet.updateVelocity(new Vector(new double[]{accelerationX, accelerationY}), deltaTime * timeFactor);
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
            
            // Draw dark space background (fallback if stars image fails to load)
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Save the original transform for text drawing
            AffineTransform originalTransform = g2d.getTransform();
            
            // Apply zoom centered on screen center
            int screenCenterX = getWidth() / 2;
            int screenCenterY = getHeight() / 2;
            g2d.translate(screenCenterX, screenCenterY);
            g2d.scale(zoomLevel, zoomLevel);
            g2d.translate(-screenCenterX, -screenCenterY);
            
            // Apply pan
            g2d.translate(panLevelX, panLevelY);
            
            // Draw stars background (tiled to cover the visible area)
            drawStarsBackground(g2d);
            
            // Draw grid background for position reference
            drawGrid(g2d);
            
            // Draw all planets
            if (planets != null) {
                for (Planet planet : planets) {
                    planet.draw(g2d);
                }
            }
            
            // Draw red X marker at last click position (in world coordinates)
            // Restore original transform for text and click marker (so they're not zoomed/panned)
            g2d.setTransform(originalTransform);
            
            // Draw click marker in screen coordinates so it stays the same size
            drawClickMarker(g2d);

            if (clickedPlanet != null) {
                Planet selectedPlanet = clickedPlanet;

                // Draw info box
                int infoX = 10;
                int infoY = 70;
                int boxWidth = 250;
                int boxHeight = 210;  // Increased height for name and temperature fields

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
                g2d.drawString(String.format("Name: %s", selectedPlanet.getName() != null ? selectedPlanet.getName() : "Unnamed"), infoX + 10, textY);
                textY += 20;
                g2d.drawString(String.format("Mass: %.2f", selectedPlanet.getMass()), infoX + 10, textY);
                textY += 20;
                g2d.drawString(String.format("Radius: %.2f", selectedPlanet.getRadius()), infoX + 10, textY);
                textY += 20;
                double[] pos = selectedPlanet.getPositionArray();
                g2d.drawString(String.format("Position: (%.1f, %.1f)", pos[0], pos[1]), 
                            infoX + 10, textY);
                textY += 20;
                double[] velocity = selectedPlanet.getVelocityArray();
                g2d.drawString(String.format("Velocity: (%.2f, %.2f)", velocity[0], velocity[1]), 
                            infoX + 10, textY);
                textY += 20;
                
                // Speed calculation
                double speed = Math.sqrt(velocity[0] * velocity[0] + 
                                        velocity[1] * velocity[1]);
                g2d.drawString(String.format("Speed: %.2f", speed), infoX + 10, textY);
                textY += 20;
                
                // Period of Rotation
                double period = selectedPlanet.getPeriodOfRotation();
                if (period > 0) {
                    g2d.drawString(String.format("Period (T): %.2f s", period), infoX + 10, textY);
                } else {
                    g2d.drawString("Period (T): Not rotating", infoX + 10, textY);
                }
                textY += 20;
                
                // Temperature
                g2d.drawString(String.format("Temperature: %.2f K", selectedPlanet.getTemperature()), infoX + 10, textY);
            }
            
            // Draw info text (always at same screen position, not affected by zoom/pan)
            g2d.setColor(Color.WHITE);
            g2d.drawString("Planets: " + (planets != null ? planets.size() : 0), 10, 20);
            g2d.drawString("G = " + gravitationalConstant, 10, 35);
            if (isPaused) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("PAUSED - Press SPACE to resume", 10, 50);
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
        private void drawStarsBackground(Graphics2D g2d) {
            if (starsBackground == null) {
                return;
            }
            
            // Calculate world bounds that are visible on screen
            // Reverse transform the screen corners to get world bounds
            double[] topLeft = screenToWorld(0, 0);
            double[] bottomRight = screenToWorld(getWidth(), getHeight());
            
            double worldLeft = topLeft[0];
            double worldTop = topLeft[1];
            double worldRight = bottomRight[0];
            double worldBottom = bottomRight[1];
            
            // Calculate how many tiles we need
            int imgWidth = starsBackground.getWidth();
            int imgHeight = starsBackground.getHeight();
            
            // Calculate starting tile position
            int startTileX = (int) Math.floor(worldLeft / imgWidth);
            int startTileY = (int) Math.floor(worldTop / imgHeight);
            int endTileX = (int) Math.ceil(worldRight / imgWidth);
            int endTileY = (int) Math.ceil(worldBottom / imgHeight);
            
            // Draw tiled background
            for (int tileY = startTileY; tileY <= endTileY; tileY++) {
                for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                    int drawX = tileX * imgWidth;
                    int drawY = tileY * imgHeight;
                    g2d.drawImage(starsBackground, drawX, drawY, null);
                }
            }
        }
        
        private void drawClickMarker(Graphics2D g2d) {
            // Convert world coordinates to screen coordinates using the same transform as drawing
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Build the same transformation as used in drawing
            AffineTransform transform = new AffineTransform();
            transform.translate(centerX, centerY);
            transform.scale(zoomLevel, zoomLevel);
            transform.translate(-centerX, -centerY);
            transform.translate(panLevelX, panLevelY);
            
            // Transform world coordinates to screen coordinates
            double[] src = {clickedWorldX, clickedWorldY};
            double[] dst = new double[2];
            transform.transform(src, 0, dst, 0, 1);
            
            double screenX = dst[0];
            double screenY = dst[1];
            
            // Size of the X marker (in screen pixels, stays constant)
            int markerSize = 15;
            int halfSize = markerSize / 2;
            
            // Set color and stroke for the X
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2.0f));
            
            // Draw the X: two diagonal lines at screen coordinates
            int x = (int) screenX;
            int y = (int) screenY;
            
            // Draw diagonal lines forming an X
            g2d.drawLine(x - halfSize, y - halfSize, x + halfSize, y + halfSize);
            g2d.drawLine(x - halfSize, y + halfSize, x + halfSize, y - halfSize);
        }
    }

}
