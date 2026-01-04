package simulations.NewtonianGravity.Gravity3D;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * OpenGL panel component that embeds an LWJGL OpenGL context within an AWT Canvas.
 * This allows 3D rendering using OpenGL while maintaining Swing UI integration.
 * 
 */
public class GLPanel extends Canvas {
    
    private long windowHandle;
    private boolean initialized = false;
    private int width;
    private int height;
    
    // Callbacks for rendering
    private Runnable renderCallback;
    private Runnable initCallback;
    
    // Track if GLFW is initialized (shared across instances)
    private static boolean glfwInitialized = false;
    
    /**
     * Creates a new OpenGL panel.
     */
    public GLPanel() {
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(100, 100));
        
        // Listen for component resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (initialized) {
                    int newWidth = getWidth();
                    int newHeight = getHeight();
                    if (newWidth > 0 && newHeight > 0 && (width != newWidth || height != newHeight)) {
                        width = newWidth;
                        height = newHeight;
                        glfwSetWindowSize(windowHandle, width, height);
                        glfwMakeContextCurrent(windowHandle);
                        glViewport(0, 0, width, height);
                    }
                }
            }
        });
    }
    
    /**
     * Initializes the OpenGL context.
     * Must be called after the component is added to a visible window.
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Initialize GLFW (only once)
            if (!glfwInitialized) {
                if (!glfwInit()) {
                    throw new IllegalStateException("Unable to initialize GLFW");
                }
                glfwInitialized = true;
            }
            
            // Configure GLFW
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Start hidden
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            
            width = getWidth() > 0 ? getWidth() : 800;
            height = getHeight() > 0 ? getHeight() : 600;
            
            // Create the window
            windowHandle = glfwCreateWindow(width, height, "OpenGL Context", NULL, NULL);
            if (windowHandle == NULL) {
                throw new RuntimeException("Failed to create GLFW window");
            }
            
            // Make the OpenGL context current
            glfwMakeContextCurrent(windowHandle);
            
            // Enable v-sync
            glfwSwapInterval(1);
            
            // Load OpenGL function pointers
            GL.createCapabilities();
            
            // Set up viewport
            glViewport(0, 0, width, height);
            
            // Enable depth testing
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            
            // Enable face culling
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            
            // Set clear color (black)
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            
            initialized = true;
            
            // Call initialization callback if set
            if (initCallback != null) {
                initCallback.run();
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize OpenGL context", e);
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
     * Renders a frame. Should be called from the animation loop.
     */
    public void render() {
        if (!initialized) {
            return;
        }
        
        // Make context current
        glfwMakeContextCurrent(windowHandle);
        
        // Check for window close
        if (glfwWindowShouldClose(windowHandle)) {
            return;
        }
        
        // Clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        // Call render callback
        if (renderCallback != null) {
            renderCallback.run();
        }
        
        // Swap buffers
        glfwSwapBuffers(windowHandle);
        
        // Poll events
        glfwPollEvents();
    }
    
    /**
     * Cleanup resources when component is removed.
     */
    public void cleanup() {
        if (initialized) {
            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(windowHandle);
            glfwDestroyWindow(windowHandle);
            
            initialized = false;
        }
    }
    
    /**
     * Static cleanup for GLFW (call when application exits).
     */
    public static void cleanupGLFW() {
        if (glfwInitialized) {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
            glfwInitialized = false;
        }
    }
    
    /**
     * Gets the OpenGL context window handle.
     */
    public long getWindowHandle() {
        return windowHandle;
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
}

