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

import static org.lwjgl.opengl.GL11.*;

/**
 * 3D Gravity Simulation - Multiple planets interacting through gravitational forces in 3D space
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
    
    /** OpenGL panel for 3D rendering */
    private GLPanel glPanel;
    
    /** Camera for 3D view */
    private Camera camera;
    
    /** Pause state */
    private boolean isPaused = false;
    
    /** Control panel for adding objects */
    private Gravity3DControlPanel controlPanel;
    
    @Override
    public void initialize() {
        setTitle("3D Gravity Simulation");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        planets = new ArrayList<>();
        
        // TODO: Initialize 3D control panel
        // controlPanel = new Gravity3DControlPanel(...);
        
        // Create and initialize OpenGL panel
        glPanel = new GLPanel();
        glPanel.setRenderCallback(this::render);
        
        setLayout(new BorderLayout());
        // add(controlPanel, BorderLayout.EAST);
        add(glPanel, BorderLayout.CENTER);
        
        setVisible(true);
        
        // Initialize OpenGL context after window is visible
        SwingUtilities.invokeLater(() -> {
            glPanel.initialize();
            camera = new Camera();
            camera.setViewport(glPanel.getGLWidth(), glPanel.getGLHeight());
            Sphere.initializeMesh(32, 16);
        });
    }
    
    @Override
    public void render() {
        if (glPanel != null && glPanel.isInitialized() && camera != null) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f viewMatrix = camera.getViewMatrix();
            Matrix4f projMatrix = camera.getProjectionMatrix();
            Matrix4f modelMatrix = new Matrix4f().translate(0, 0, -5).scale(1.0f);

            Sphere.render(modelMatrix, viewMatrix, projMatrix, new Vector3f(1.0f, 0.0f, 0.0f));
        }
    }
    
    @Override
    public void update(double deltaTime) {
        if (isPaused) {
            return;
        }
        
        // TODO: Implement 3D physics update
    }
    
    
    @Override
    public void start() {
        initialize();
        
        animationTimer = new Timer(16, e -> {
            update(DELTA_TIME);
            if (glPanel != null && glPanel.isInitialized()) {
                glPanel.render();
            }
        });
        
        animationTimer.start();
    }
    
    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (glPanel != null) {
            glPanel.cleanup();
        }
        GLPanel.cleanupGLFW();
        dispose();
        Sphere.cleanupStatic();
    }
}

