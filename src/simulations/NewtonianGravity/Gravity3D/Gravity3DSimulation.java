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

import com.jogamp.opengl.GL3;

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
        
        // Set initialization callback
        glPanel.setInitCallback(() -> {
            GL3 gl = glPanel.getGL();
            if (gl != null) {
                camera = new Camera();
                camera.setViewport(glPanel.getGLWidth(), glPanel.getGLHeight());
                Sphere.initializeMesh(gl, 32, 16);
                // Start animator after context is initialized
                glPanel.startAnimator();
            }
        });
        
        // Set render callback
        glPanel.setRenderCallback(this::render);
        
        setLayout(new BorderLayout());
        // add(controlPanel, BorderLayout.EAST);
        add(glPanel, BorderLayout.CENTER);
        
        setVisible(true);
        
        // Wait for OpenGL context to initialize before starting animator
        // The init() callback will be called by JOGL when the context is ready
    }
    
    @Override
    public void render() {
        GL3 gl = glPanel.getGL();
        if (gl != null && glPanel.isInitialized() && camera != null) {
            Matrix4f viewMatrix = camera.getViewMatrix();
            Matrix4f projMatrix = camera.getProjectionMatrix();
            Matrix4f modelMatrix = new Matrix4f().translate(0, 0, -5).scale(1.0f);

            Sphere.render(gl, modelMatrix, viewMatrix, projMatrix, new Vector3f(1.0f, 0.0f, 0.0f));
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
        // Animator will be started in the init callback after OpenGL context is ready
    }
    
    @Override
    public void stop() {
        if (glPanel != null) {
            GL3 gl = glPanel.getGL();
            if (gl != null) {
                Sphere.cleanupStatic(gl);
            }
            glPanel.cleanup();
        }
        dispose();
    }
}
