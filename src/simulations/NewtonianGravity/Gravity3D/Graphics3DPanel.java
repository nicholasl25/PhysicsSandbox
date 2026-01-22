package simulations.NewtonianGravity.Gravity3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D rendering panel using Graphics2D with 3D-to-2D projection.
 * This avoids all native dependencies and threading issues.
 */
public class Graphics3DPanel extends JPanel {
    
    private Camera camera;
    private List<java.util.function.Consumer<Graphics2D>> renderCallbacks = new ArrayList<>();
    
    // Mouse drag state for camera rotation
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    private boolean isDragging = false;
    
    public boolean isDragging() {
        return isDragging;
    }
    
    // Rotation sensitivity
    private static final float ROTATION_SENSITIVITY = 0.005f;
    
    // Zoom limits
    private static final float MIN_FOV = (float) Math.toRadians(10.0);
    private static final float MAX_FOV = (float) Math.toRadians(120.0);
    private static final float ZOOM_SENSITIVITY = 0.1f;
    
    public Graphics3DPanel() {
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(100, 100));
        setBackground(Color.BLACK);
        
        // Add mouse listeners for camera control
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (camera != null && SwingUtilities.isLeftMouseButton(e)) {
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    isDragging = true;
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = false;
                    lastMouseX = -1;
                    lastMouseY = -1;
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (camera != null && isDragging && SwingUtilities.isLeftMouseButton(e)) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;
                    
                    // Rotate camera based on mouse movement
                    float deltaYaw = -dx * ROTATION_SENSITIVITY;
                    float deltaPitch = -dy * ROTATION_SENSITIVITY;
                    
                    camera.rotate(deltaYaw, deltaPitch);
                    
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    
                    // Trigger repaint to show updated camera view
                    repaint();
                }
            }
        });
        
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (camera != null) {
                    int rotation = e.getWheelRotation();
                    float currentFOV = camera.getFOV();
                    float newFOV = currentFOV + rotation * ZOOM_SENSITIVITY;
                    
                    // Clamp FOV to limits
                    if (newFOV < MIN_FOV) newFOV = MIN_FOV;
                    if (newFOV > MAX_FOV) newFOV = MAX_FOV;
                    
                    camera.setFOV(newFOV);
                    
                    // Trigger repaint to show updated zoom
                    repaint();
                }
            }
        });
    }
    
    public void setCamera(Camera camera) {
        this.camera = camera;
        if (camera != null) {
            camera.setViewport(getWidth(), getHeight());
        }
    }
    
    public void addRenderCallback(java.util.function.Consumer<Graphics2D> callback) {
        renderCallbacks.add(callback);
    }
    
    public void addRenderCallback(Runnable callback) {
        // Compatibility wrapper for Runnable callbacks
        renderCallbacks.add(g2d -> callback.run());
    }
    
    public void removeRenderCallback(java.util.function.Consumer<Graphics2D> callback) {
        renderCallbacks.remove(callback);
    }
    
    public boolean isInitialized() {
        return camera != null;
    }
    
    public int getGLWidth() {
        return getWidth() > 0 ? getWidth() : 800;
    }
    
    public int getGLHeight() {
        return getHeight() > 0 ? getHeight() : 600;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Clear background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        if (camera != null) {
            // Update camera viewport if needed
            camera.setViewport(getWidth(), getHeight());
            
            // Call render callbacks with Graphics2D
            for (java.util.function.Consumer<Graphics2D> callback : renderCallbacks) {
                callback.accept(g2d);
            }
        }
    }
    
    /**
     * Renders the frame by triggering a repaint.
     */
    public void render() {
        repaint();
    }
    
    /**
     * Cleanup (no-op for Graphics2D, but kept for API compatibility).
     */
    public void cleanup() {
        // Nothing to clean up for Graphics2D
    }
}
