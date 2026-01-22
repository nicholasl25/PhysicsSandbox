package simulations.NewtonianGravity.Gravity3D;

import simulations.BaseSimulation;
import simulations.NewtonianGravity.Planet;
import simulations.NewtonianGravity.PointMass;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
    
    /** Stars background image */
    private BufferedImage starsBackground;
    
    /** Selected planet */
    private Planet selectedPlanet = null;
    
    @Override
    public void initialize() {
        setTitle("Gravity 3D Simulation");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        planets = new ArrayList<>();
        
        // Load stars background
        loadStarsBackground();
        
        // Initialize 3D control panel
        controlPanel = new Gravity3DControlPanel(
            () -> addPlanetFromFields(),
            () -> clearSimulation(),
            (g) -> updateGravity(g),
            (tf) -> updateTimeFactor(tf),
            (b) -> updateBounce(b),
            (rk4) -> updateRK4(rk4)
        );
        
        // Create Graphics2D panel (no native dependencies!)
        graphicsPanel = new Graphics3DPanel();
        
        // Create camera
        camera = new Camera();
        camera.setPosition(0, 0, 200); // Position camera at (0, 0, 200) looking at origin
        graphicsPanel.setCamera(camera);
        
        // Initialize sphere mesh (lower resolution for better performance)
        SphereRenderer.initializeMesh(16, 8);
        
        // Add render callback - Graphics2D will be provided by paintComponent
        graphicsPanel.addRenderCallback(g2d -> {
            if (camera != null) {
                renderToGraphics(g2d);
            }
        });
        
        // Add click listener for planet selection
        graphicsPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (!graphicsPanel.isDragging() && SwingUtilities.isLeftMouseButton(e)) {
                    handleClick(e.getX(), e.getY());
                }
            }
        });
        
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.EAST);
        add(graphicsPanel, BorderLayout.CENTER);
        
        // Set up spacebar key binding for pause/resume
        setupKeyBindings();
        
        setVisible(true);
        
        // Create initial planets
        setupPlanets();
    }
    
    /**
     * Sets up key bindings for pause/resume.
     */
    private void setupKeyBindings() {
        JRootPane rootPane = getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        AbstractAction pauseResumeAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                isPaused = !isPaused;
            }
        };
        
        KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        inputMap.put(spaceKey, "pauseResume");
        actionMap.put("pauseResume", pauseResumeAction);
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
        
        camera.setViewport(graphicsPanel.getGLWidth(), graphicsPanel.getGLHeight());
        
        // Draw stars background
        drawStarsBackground(g2d);
        
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();
        
        for (Planet planet : planets) {
            Vector pos = planet.getPosition();
            float x = (float)pos.get(0);
            float y = (float)pos.get(1);
            // Handle 2D planets (z = 0) or 3D planets
            float z = pos.dimensions() > 2 ? (float)pos.get(2) : 0.0f;
            float r = (float)planet.getRadius();
            Color color = planet.getColor();
            Vector3f colorVec = new Vector3f(color.getRed()/255.0f, color.getGreen()/255.0f, color.getBlue()/255.0f);

            Matrix4f modelMatrix = new Matrix4f().translate(x, y, z).scale(r);
            
            // Get texture and rotation angle
            java.awt.image.BufferedImage texture = planet.getTexture();
            double rotationAngle = planet.getRotationAngle();

            SphereRenderer.render(g2d, modelMatrix, viewMatrix, projMatrix, colorVec,
                graphicsPanel.getGLWidth(), graphicsPanel.getGLHeight(),
                texture, rotationAngle);
        }
        
        // Draw selection highlight
        if (selectedPlanet != null) {
            Vector pos = selectedPlanet.getPosition();
            float x = (float)pos.get(0);
            float y = (float)pos.get(1);
            float z = pos.dimensions() > 2 ? (float)pos.get(2) : 0.0f;
            float r = (float)selectedPlanet.getRadius();
            
            Vector3f worldPos = new Vector3f(x, y, z);
            java.awt.Point screenPos = projectToScreen(worldPos, viewMatrix, projMatrix);
            if (screenPos != null) {
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.setColor(Color.YELLOW);
                int screenRadius = (int)(r * 50.0f / Math.max(1.0f, z + 200.0f)); // Approximate radius on screen
                g2d.drawOval(screenPos.x - screenRadius - 5, screenPos.y - screenRadius - 5, 
                            (screenRadius + 5) * 2, (screenRadius + 5) * 2);
            }
        }
        
        // Draw pause indicator
        if (isPaused) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Sans-serif", Font.BOLD, 24));
            g2d.drawString("PAUSED - Press SPACE to resume", 10, 50);
        }
    }
    
    /**
     * Projects a 3D point to 2D screen coordinates.
     */
    private java.awt.Point projectToScreen(Vector3f worldPos, Matrix4f viewMatrix, Matrix4f projMatrix) {
        Vector4f viewPos = new Vector4f(worldPos, 1.0f);
        viewMatrix.transform(viewPos);
        
        if (viewPos.z > -0.1f) {
            return null;
        }
        
        Vector4f clipPos = new Vector4f(viewPos);
        projMatrix.transform(clipPos);
        
        if (Math.abs(clipPos.w) < 0.0001f) {
            return null;
        }
        
        float x = clipPos.x / clipPos.w;
        float y = clipPos.y / clipPos.w;
        
        if (x < -1 || x > 1 || y < -1 || y > 1) {
            return null;
        }
        
        int screenX = (int) ((x + 1.0f) * graphicsPanel.getGLWidth() / 2.0f);
        int screenY = (int) ((1.0f - y) * graphicsPanel.getGLHeight() / 2.0f);
        
        return new java.awt.Point(screenX, screenY);
    }
    
    /**
     * Handles mouse click for planet selection.
     */
    private void handleClick(int screenX, int screenY) {
        if (camera == null) return;
        
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();
        
        Planet clickedPlanet = null;
        double minDist = Double.MAX_VALUE;
        
        for (Planet planet : planets) {
            Vector pos = planet.getPosition();
            float x = (float)pos.get(0);
            float y = (float)pos.get(1);
            float z = pos.dimensions() > 2 ? (float)pos.get(2) : 0.0f;
            
            Vector3f worldPos = new Vector3f(x, y, z);
            java.awt.Point screenPos = projectToScreen(worldPos, viewMatrix, projMatrix);
            
            if (screenPos != null) {
                double dx = screenX - screenPos.x;
                double dy = screenY - screenPos.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                float r = (float)planet.getRadius();
                int screenRadius = (int)(r * 50.0f / Math.max(1.0f, z + 200.0f));
                
                if (dist <= screenRadius && dist < minDist) {
                    clickedPlanet = planet;
                    minDist = dist;
                }
            }
        }
        
        if (selectedPlanet != null) {
            selectedPlanet.clicked();
        }
        selectedPlanet = clickedPlanet;
        if (selectedPlanet != null) {
            selectedPlanet.clicked();
        }
        graphicsPanel.repaint();
    }
    
    /**
     * Loads the stars background image.
     */
    private void loadStarsBackground() {
        try {
            File starsFile = new File("resources/textures/Stars.png");
            starsBackground = ImageIO.read(starsFile);
        } catch (IOException e) {
            starsBackground = null;
        }
    }
    
    /**
     * Draws the stars background.
     */
    private void drawStarsBackground(Graphics2D g2d) {
        if (starsBackground == null) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, graphicsPanel.getGLWidth(), graphicsPanel.getGLHeight());
            return;
        }
        
        int imgWidth = starsBackground.getWidth();
        int imgHeight = starsBackground.getHeight();
        
        // Simple tiling - cover the screen
        for (int y = 0; y < graphicsPanel.getGLHeight(); y += imgHeight) {
            for (int x = 0; x < graphicsPanel.getGLWidth(); x += imgWidth) {
                g2d.drawImage(starsBackground, x, y, null);
            }
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
                totalForce.addto(forceVec);
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
    
    
    /**
     * Adds a planet using values from the control panel
     */
    private void addPlanetFromFields() {
        Gravity3DControlPanel.PlanetData data = controlPanel.getPlanetData();
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Determine planet name: use provided name or generate "Planet #N" or "PointMass #N"
        String planetName;
        if (data.name != null && !data.name.trim().isEmpty()) {
            planetName = data.name.trim();
        } else {
            if (data.fixedLocation) {
                planetName = "PointMass #" + planets.size();
            } else {
                planetName = "Planet #" + planets.size();
            }
        }
        
        Planet newObject;
        double angularVelocity = data.getAngularVelocity();
        
        if (data.fixedLocation) {
            // For 3D, create a Planet with zero velocity (effectively stationary)
            // PointMass only supports 2D, so we use Planet with zero velocity
            newObject = new Planet(
                3, // dimension
                data.mass,
                data.radius,
                new Vector(new double[]{data.x, data.y, data.z}), // position
                new Vector(new double[]{0.0, 0.0, 0.0}), // zero velocity (stationary)
                angularVelocity,
                data.temperature,
                data.color,
                data.texturePath,
                planetName
            );
            // PointMass uses 2D constructor, but we need to set 3D position
            // We'll need to create it with 3D position - but PointMass constructor is 2D only
            // For now, create it at (x, y) and we'll need to handle z separately
            // Actually, let's check if we can use the 3D Planet constructor with zero velocity
        } else {
            // Create a Planet with 3D position and velocity
            newObject = new Planet(
                3, // dimension
                data.mass,
                data.radius,
                new Vector(new double[]{data.x, data.y, data.z}), // position
                new Vector(new double[]{data.vx, data.vy, data.vz}), // velocity
                angularVelocity,
                data.temperature,
                data.color,
                data.texturePath,
                planetName
            );
        }
        
        planets.add(newObject);
        graphicsPanel.repaint();
    }
    
    /**
     * Clears all planets from the simulation
     */
    private void clearSimulation() {
        planets.clear();
        graphicsPanel.repaint();
    }
    
    /**
     * Updates the gravitational constant from the slider
     */
    private void updateGravity(Double newGravity) {
        gravitationalConstant = newGravity;
    }
    
    /**
     * Updates the time factor from the slider
     */
    private void updateTimeFactor(Double newTimeFactor) {
        timeFactor = newTimeFactor;
    }
    
    /**
     * Updates the bounce setting from the checkbox
     */
    private void updateBounce(Boolean newBounce) {
        bounce = newBounce;
    }
    
    /**
     * Updates the RK4 setting from the checkbox
     */
    private void updateRK4(Boolean newRK4) {
        useRK4 = newRK4;
    }
    
    /**
     * Creates initial planets for testing the 3D simulation.
     */
    private void setupPlanets() {
        // Create a sun at the origin
        Planet sun = new Planet(
            3, // dimension
            1000.0, // mass
            20.0, // radius
            new Vector(new double[]{0.0, 0.0, 0.0}), // position
            new Vector(new double[]{0.0, 0.0, 0.0}), // velocity
            0.02, // angularVelocity
            5778.0, // temperature
            Color.YELLOW,
            "resources/textures/Sun.jpg",
            "Sun"
        );
        
        // Create a planet orbiting the sun
        Planet planet1 = new Planet(
            3, // dimension
            50.0, // mass
            20.0, // radius
            new Vector(new double[]{60.0, 20.0, 0.0}), // position (to the right of sun)
            new Vector(new double[]{0.0, -8.0, 0.0}), // velocity (orbital motion)
            0.06, // angularVelocity
            288.0, // temperature
            Color.BLUE,
            "resources/textures/Earth.jpg",
            "Earth"
        );
        
        planets.add(sun);
        planets.add(planet1);
    }
    
    @Override
    public void start() {
        initialize();
        
        animationTimer = new Timer(16, e -> {
            update(DELTA_TIME);
            render(); // This triggers repaint
        });
        
        animationTimer.start();
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

