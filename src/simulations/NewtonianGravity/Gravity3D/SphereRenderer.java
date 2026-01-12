package simulations.NewtonianGravity.Gravity3D;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Renders 3D spheres using Graphics2D with 3D-to-2D projection.
 * No OpenGL dependencies - pure Java rendering.
 */
public class SphereRenderer {
    
    // Sphere mesh data
    private static class SphereMesh {
        List<Vector3f> vertices = new ArrayList<>();
        List<int[]> triangles = new ArrayList<>();
    }
    
    private static SphereMesh mesh;
    private static boolean meshInitialized = false;
    
    /**
     * Initialize sphere mesh geometry.
     */
    public static void initializeMesh(int segments, int rings) {
        if (meshInitialized) {
            return;
        }
        
        System.out.println("[SphereRenderer] Initializing sphere mesh (" + segments + "x" + rings + ")...");
        mesh = new SphereMesh();
        
        // Generate vertices
        for (int i = 0; i <= rings; i++) {
            for (int j = 0; j <= segments; j++) {
                double phi = Math.PI * ((double) i / rings);
                double theta = 2 * Math.PI * ((double) j / segments);
                
                float x = (float) (Math.sin(phi) * Math.cos(theta));
                float y = (float) Math.cos(phi);
                float z = (float) (Math.sin(phi) * Math.sin(theta));
                
                mesh.vertices.add(new Vector3f(x, y, z));
            }
        }
        
        // Generate triangles
        for (int i = 0; i < rings; i++) {
            for (int j = 0; j < segments; j++) {
                int topLeft = i * (segments + 1) + j;
                int topRight = i * (segments + 1) + j + 1;
                int bottomLeft = (i + 1) * (segments + 1) + j;
                int bottomRight = (i + 1) * (segments + 1) + j + 1;
                
                // Two triangles per quad
                mesh.triangles.add(new int[]{topLeft, topRight, bottomLeft});
                mesh.triangles.add(new int[]{topRight, bottomRight, bottomLeft});
            }
        }
        
        meshInitialized = true;
        System.out.println("[SphereRenderer] Mesh initialized: " + mesh.vertices.size() + " vertices, " + 
                          mesh.triangles.size() + " triangles");
    }
    
    /**
     * Projects a 3D point to 2D screen coordinates.
     */
    private static Point projectPoint(Vector3f worldPos, Matrix4f viewMatrix, Matrix4f projMatrix, 
                                     int screenWidth, int screenHeight) {
        // Transform to view space
        Vector4f viewPos = new Vector4f(worldPos, 1.0f);
        viewMatrix.transform(viewPos);
        
        // Check if behind camera
        if (viewPos.z > -0.1f) {
            return null; // Behind or too close to camera
        }
        
        // Apply projection
        Vector4f clipPos = new Vector4f(viewPos);
        projMatrix.transform(clipPos);
        
        // Perspective divide
        if (Math.abs(clipPos.w) < 0.0001f) {
            return null;
        }
        
        float x = clipPos.x / clipPos.w;
        float y = clipPos.y / clipPos.w;
        float z = clipPos.z / clipPos.w;
        
        // Clamp to [-1, 1] (clip space)
        if (x < -1 || x > 1 || y < -1 || y > 1 || z < -1 || z > 1) {
            return null; // Outside view frustum
        }
        
        // Convert to screen coordinates
        int screenX = (int) ((x + 1.0f) * screenWidth / 2.0f);
        int screenY = (int) ((1.0f - y) * screenHeight / 2.0f); // Flip Y axis
        
        return new Point(screenX, screenY);
    }
    
    /**
     * Renders a sphere using Graphics2D.
     */
    public static void render(Graphics2D g2d, Matrix4f modelMatrix, Matrix4f viewMatrix, 
                             Matrix4f projectionMatrix, Vector3f color, int screenWidth, int screenHeight) {
        if (!meshInitialized || mesh == null) {
            return;
        }
        
        // Combine model and view matrices
        Matrix4f mvp = new Matrix4f(projectionMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(modelMatrix);
        
        // Store projected points for triangles
        List<Triangle2D> triangles2D = new ArrayList<>();
        
        // Project all vertices
        Vector3f[] projectedVerts = new Vector3f[mesh.vertices.size()];
        float[] depths = new float[mesh.vertices.size()];
        
        for (int i = 0; i < mesh.vertices.size(); i++) {
            Vector3f worldPos = new Vector3f(mesh.vertices.get(i));
            modelMatrix.transformPosition(worldPos);
            
            Point screenPos = projectPoint(worldPos, viewMatrix, projectionMatrix, screenWidth, screenHeight);
            if (screenPos != null) {
                projectedVerts[i] = new Vector3f(screenPos.x, screenPos.y, worldPos.z);
                depths[i] = worldPos.z;
            }
        }
        
        // Build triangles with depth for sorting
        for (int[] tri : mesh.triangles) {
            if (projectedVerts[tri[0]] != null && projectedVerts[tri[1]] != null && projectedVerts[tri[2]] != null) {
                float avgDepth = (depths[tri[0]] + depths[tri[1]] + depths[tri[2]]) / 3.0f;
                triangles2D.add(new Triangle2D(
                    (int) projectedVerts[tri[0]].x, (int) projectedVerts[tri[0]].y,
                    (int) projectedVerts[tri[1]].x, (int) projectedVerts[tri[1]].y,
                    (int) projectedVerts[tri[2]].x, (int) projectedVerts[tri[2]].y,
                    avgDepth
                ));
            }
        }
        
        // Sort by depth (back to front for proper rendering)
        triangles2D.sort((a, b) -> Float.compare(b.depth, a.depth));
        
        // Draw triangles
        g2d.setColor(new Color(color.x, color.y, color.z));
        for (Triangle2D tri : triangles2D) {
            Path2D path = new Path2D.Float();
            path.moveTo(tri.x1, tri.y1);
            path.lineTo(tri.x2, tri.y2);
            path.lineTo(tri.x3, tri.y3);
            path.closePath();
            g2d.fill(path);
        }
        
        // Draw wireframe (optional, for debugging)
        // g2d.setColor(Color.WHITE);
        // g2d.setStroke(new BasicStroke(1));
        // for (Triangle2D tri : triangles2D) {
        //     g2d.drawLine(tri.x1, tri.y1, tri.x2, tri.y2);
        //     g2d.drawLine(tri.x2, tri.y2, tri.x3, tri.y3);
        //     g2d.drawLine(tri.x3, tri.y3, tri.x1, tri.y1);
        // }
    }
    
    /**
     * Simple 2D triangle with depth for sorting.
     */
    private static class Triangle2D {
        int x1, y1, x2, y2, x3, y3;
        float depth;
        
        Triangle2D(int x1, int y1, int x2, int y2, int x3, int y3, float depth) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.x3 = x3; this.y3 = y3;
            this.depth = depth;
        }
    }
    
    /**
     * Cleanup (no-op for Graphics2D).
     */
    public static void cleanupStatic() {
        mesh = null;
        meshInitialized = false;
    }
}
