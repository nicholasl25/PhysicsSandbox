package simulations.NewtonianGravity.Gravity3D;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * OpenGL panel component using JOGL (Java OpenGL).
 * This allows 3D rendering using OpenGL while maintaining Swing UI integration.
 * JOGL creates OpenGL contexts directly on AWT components without needing GLFW.
 */
public class GLPanel extends GLCanvas implements GLEventListener {
    
    private boolean initialized = false;
    private int width;
    private int height;
    
    // Store GL3 context for callbacks
    private GL3 gl;
    
    // Callbacks for rendering
    private Runnable renderCallback;
    private Runnable initCallback;
    
    // Animator for rendering loop
    private FPSAnimator animator;
    
    /**
     * Creates a new OpenGL panel.
     */
    public GLPanel() {
        // Try to get OpenGL 3.3 profile, fallback to GL2 if not available
        GLProfile profile = null;
        try {
            profile = GLProfile.get(GLProfile.GL3);
        } catch (Exception e) {
            System.err.println("GL3 profile not available, trying GL2: " + e.getMessage());
            try {
                profile = GLProfile.get(GLProfile.GL2);
            } catch (Exception e2) {
                throw new RuntimeException("No OpenGL profile available", e2);
            }
        }
        
        if (profile == null) {
            throw new RuntimeException("Failed to get OpenGL profile");
        }
        
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setDoubleBuffered(true);
        capabilities.setHardwareAccelerated(true);
        
        // Set preferred size
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(100, 100));
        
        // Add this as a GLEventListener
        addGLEventListener(this);
        
        // Listen for component resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (initialized && gl != null) {
                    width = getWidth();
                    height = getHeight();
                }
            }
        });
    }
    
    /**
     * Initializes the OpenGL context.
     * This is called automatically by JOGL when the component is first displayed.
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        try {
            GL glBase = drawable.getGL();
            if (glBase == null) {
                System.err.println("ERROR: GL context is null in init()");
                return;
            }
            
            // Try to get GL3, but handle gracefully if not available
            try {
                this.gl = glBase.getGL3();
            } catch (Exception e) {
                System.err.println("GL3 not available: " + e.getMessage());
                // Try GL2 as fallback
                try {
                    GL2 gl2 = glBase.getGL2();
                    if (gl2 != null) {
                        System.err.println("Using GL2 instead of GL3");
                        // We'll need to handle this differently, but for now just fail
                        throw new RuntimeException("GL3 required but not available");
                    }
                } catch (Exception e2) {
                    throw new RuntimeException("No OpenGL context available", e2);
                }
            }
            
            if (this.gl == null) {
                System.err.println("ERROR: GL3 context is null");
                return;
            }
            
            width = getWidth() > 0 ? getWidth() : 800;
            height = getHeight() > 0 ? getHeight() : 600;
            
            // Set up viewport
            this.gl.glViewport(0, 0, width, height);
            
            // Enable depth testing
            this.gl.glEnable(GL3.GL_DEPTH_TEST);
            this.gl.glDepthFunc(GL3.GL_LEQUAL);
            
            // Enable face culling
            this.gl.glEnable(GL3.GL_CULL_FACE);
            this.gl.glCullFace(GL3.GL_BACK);
            
            // Set clear color (black)
            this.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            
            initialized = true;
            
            // Call initialization callback if set (GL is now available)
            if (initCallback != null) {
                initCallback.run();
            }
        } catch (Exception e) {
            System.err.println("Error in GLPanel.init(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize OpenGL context", e);
        }
    }
    
    /**
     * Called when the OpenGL context is resized.
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        if (gl != null) {
            this.width = width;
            this.height = height;
            gl.glViewport(0, 0, width, height);
        }
    }
    
    /**
     * Called each frame for rendering.
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        if (!initialized || gl == null) {
            return;
        }
        
        // Clear the framebuffer
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
        
        // Call render callback
        if (renderCallback != null) {
            renderCallback.run();
        }
    }
    
    /**
     * Called when the OpenGL context is disposed.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) {
        initialized = false;
        gl = null;
        if (animator != null) {
            animator.stop();
            animator = null;
        }
    }
    
    /**
     * Sets the callback to be called during initialization.
     */
    public void setInitCallback(Runnable callback) {
        this.initCallback = callback;
    }
    
    /**
     * Sets the callback to be called each frame for rendering.
     */
    public void setRenderCallback(Runnable callback) {
        this.renderCallback = callback;
    }
    
    /**
     * Starts the rendering loop.
     * Should only be called after the component is visible and initialized.
     */
    public void startAnimator() {
        if (animator == null && initialized && gl != null) {
            animator = new FPSAnimator(this, 60);
            animator.start();
        } else if (!initialized) {
            System.err.println("Warning: Cannot start animator - OpenGL context not initialized");
        }
    }
    
    /**
     * Stops the rendering loop.
     */
    public void stopAnimator() {
        if (animator != null) {
            animator.stop();
            animator = null;
        }
    }
    
    /**
     * Renders a frame. Can be called manually if not using animator.
     */
    public void render() {
        if (initialized) {
            display();
        }
    }
    
    /**
     * Cleanup resources when component is removed.
     */
    public void cleanup() {
        stopAnimator();
        removeGLEventListener(this);
        initialized = false;
    }
    
    /**
     * Gets the OpenGL context.
     */
    public GL3 getGL() {
        return gl;
    }
    
    /**
     * Checks if the OpenGL context is initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets the current width of the OpenGL context.
     */
    public int getGLWidth() {
        return width;
    }
    
    /**
     * Gets the current height of the OpenGL context.
     */
    public int getGLHeight() {
        return height;
    }
    
    /**
     * Static cleanup (no-op for JOGL, contexts are managed per component).
     */
    public static void cleanupGLFW() {
        // JOGL doesn't need global cleanup like GLFW
    }
}
