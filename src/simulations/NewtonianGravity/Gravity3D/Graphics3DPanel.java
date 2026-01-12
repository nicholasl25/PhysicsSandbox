package simulations.NewtonianGravity.Gravity3D;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D rendering panel using Graphics2D with 3D-to-2D projection.
 * This avoids all native dependencies and threading issues.
 */
public class Graphics3DPanel extends JPanel {
    
    private Camera camera;
    private List<java.util.function.Consumer<Graphics2D>> renderCallbacks = new ArrayList<>();
    
    public Graphics3DPanel() {
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(100, 100));
        setBackground(Color.BLACK);
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
