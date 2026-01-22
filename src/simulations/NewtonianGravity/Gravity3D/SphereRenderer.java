package simulations.NewtonianGravity.Gravity3D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
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
        List<float[]> texCoords = new ArrayList<>();
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
        
        mesh = new SphereMesh();
        
        // Generate vertices
        for (int i = 0; i <= rings; i++) {
            for (int j = 0; j <= segments; j++) {
                double phi = Math.PI * ((double) i / rings);
                double theta = 2 * Math.PI * ((double) j / segments);
                
                float x = (float) (Math.sin(phi) * Math.cos(theta));
                float y = (float) Math.cos(phi);
                float z = (float) (Math.sin(phi) * Math.sin(theta));
                float u = (float) j / segments;
                float v = (float) i / rings;
                mesh.vertices.add(new Vector3f(x, y, z));
                mesh.texCoords.add(new float[]{u, v});
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
                             Matrix4f projectionMatrix, Vector3f color, int screenWidth, int screenHeight,
                             BufferedImage texture, double rotationAngle) {
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
        
        // Build triangles with depth and UV coordinates for sorting
        for (int[] tri : mesh.triangles) {
            if (projectedVerts[tri[0]] != null && projectedVerts[tri[1]] != null && projectedVerts[tri[2]] != null) {
                float avgDepth = (depths[tri[0]] + depths[tri[1]] + depths[tri[2]]) / 3.0f;
                
                // Get UV coordinates for this triangle
                float[] uv0 = mesh.texCoords.get(tri[0]);
                float[] uv1 = mesh.texCoords.get(tri[1]);
                float[] uv2 = mesh.texCoords.get(tri[2]);
                
                triangles2D.add(new Triangle2D(
                    (int) projectedVerts[tri[0]].x, (int) projectedVerts[tri[0]].y,
                    (int) projectedVerts[tri[1]].x, (int) projectedVerts[tri[1]].y,
                    (int) projectedVerts[tri[2]].x, (int) projectedVerts[tri[2]].y,
                    avgDepth,
                    uv0[0], uv0[1], uv1[0], uv1[1], uv2[0], uv2[1]
                ));
            }
        }
        
        // Sort by depth (back to front for proper rendering)
        triangles2D.sort((a, b) -> Float.compare(b.depth, a.depth));
        
        // Draw triangles
        if (texture != null) {
            // Use simpler texture rendering for better performance
            drawTexturedSphere(g2d, triangles2D, texture, rotationAngle, screenWidth, screenHeight);
        } else {
            // Draw solid color triangles
            g2d.setColor(new Color(color.x, color.y, color.z));
            for (Triangle2D tri : triangles2D) {
                Path2D path = new Path2D.Float();
                path.moveTo(tri.x1, tri.y1);
                path.lineTo(tri.x2, tri.y2);
                path.lineTo(tri.x3, tri.y3);
                path.closePath();
                g2d.fill(path);
            }
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
     * Draws a textured sphere using a simpler, faster approach.
     * Projects the texture onto the sphere's bounding circle for better performance.
     */
    private static void drawTexturedSphere(Graphics2D g2d, List<Triangle2D> triangles, BufferedImage texture, 
                                          double rotationAngle, int screenWidth, int screenHeight) {
        if (triangles.isEmpty()) return;
        
        // Find the center and approximate radius of the sphere on screen
        float centerX = 0, centerY = 0;
        float maxDist = 0;
        for (Triangle2D tri : triangles) {
            centerX += (tri.x1 + tri.x2 + tri.x3) / 3.0f;
            centerY += (tri.y1 + tri.y2 + tri.y3) / 3.0f;
        }
        centerX /= triangles.size();
        centerY /= triangles.size();
        
        for (Triangle2D tri : triangles) {
            float dist1 = (float)Math.sqrt((tri.x1 - centerX) * (tri.x1 - centerX) + (tri.y1 - centerY) * (tri.y1 - centerY));
            float dist2 = (float)Math.sqrt((tri.x2 - centerX) * (tri.x2 - centerX) + (tri.y2 - centerY) * (tri.y2 - centerY));
            float dist3 = (float)Math.sqrt((tri.x3 - centerX) * (tri.x3 - centerX) + (tri.y3 - centerY) * (tri.y3 - centerY));
            maxDist = Math.max(maxDist, Math.max(dist1, Math.max(dist2, dist3)));
        }
        
        int radius = (int)maxDist;
        if (radius < 5) {
            // Too small, just draw solid color
            return;
        }
        
        // Draw all triangles first with solid color (for depth)
        g2d.setColor(new Color(0.3f, 0.3f, 0.3f));
        for (Triangle2D tri : triangles) {
            Path2D path = new Path2D.Float();
            path.moveTo(tri.x1, tri.y1);
            path.lineTo(tri.x2, tri.y2);
            path.lineTo(tri.x3, tri.y3);
            path.closePath();
            g2d.fill(path);
        }
        
        // Draw texture as a rotated circle over the sphere (much faster)
        Shape oldClip = g2d.getClip();
        java.awt.geom.Ellipse2D.Float circle = new java.awt.geom.Ellipse2D.Float(
            centerX - radius, centerY - radius, radius * 2, radius * 2);
        g2d.setClip(circle);
        
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(centerX, centerY);
        g2d.rotate(rotationAngle);
        g2d.translate(-centerX, -centerY);
        
        // Scale texture to fit
        int texSize = Math.min(texture.getWidth(), texture.getHeight());
        g2d.drawImage(texture, 
            (int)(centerX - radius), (int)(centerY - radius),
            (int)(centerX + radius), (int)(centerY + radius),
            0, 0, texSize, texSize, null);
        
        g2d.setTransform(oldTransform);
        g2d.setClip(oldClip);
    }
    
    /**
     * Draws a textured triangle using UV coordinates (slower, more accurate).
     * Kept for reference but not used for performance reasons.
     */
    @SuppressWarnings("unused")
    private static void drawTexturedTriangle(Graphics2D g2d, Triangle2D tri, BufferedImage texture, double rotationAngle) {
        // Apply rotation to UV coordinates (rotate around U axis)
        float u0 = (float)(tri.u0 + rotationAngle / (2 * Math.PI));
        float u1 = (float)(tri.u1 + rotationAngle / (2 * Math.PI));
        float u2 = (float)(tri.u2 + rotationAngle / (2 * Math.PI));
        
        // Wrap UV coordinates
        u0 = u0 - (float)Math.floor(u0);
        u1 = u1 - (float)Math.floor(u1);
        u2 = u2 - (float)Math.floor(u2);
        
        int texWidth = texture.getWidth();
        int texHeight = texture.getHeight();
        
        // Convert UV to texture pixel coordinates
        float tx0 = u0 * texWidth;
        float ty0 = tri.v0 * texHeight;
        float tx1 = u1 * texWidth;
        float ty1 = tri.v1 * texHeight;
        float tx2 = u2 * texWidth;
        float ty2 = tri.v2 * texHeight;
        
        // Create triangle path for clipping
        Path2D path = new Path2D.Float();
        path.moveTo(tri.x1, tri.y1);
        path.lineTo(tri.x2, tri.y2);
        path.lineTo(tri.x3, tri.y3);
        path.closePath();
        
        // Set clip to triangle shape
        Shape oldClip = g2d.getClip();
        g2d.setClip(path);
        
        // Create AffineTransform to map texture triangle to screen triangle
        // We need to solve: screen = transform * texture
        // Using three point mapping: map (tx0,ty0)->(x1,y1), (tx1,ty1)->(x2,y2), (tx2,ty2)->(x3,y3)
        
        // Calculate vectors from first point
        float texDx1 = tx1 - tx0;
        float texDy1 = ty1 - ty0;
        float texDx2 = tx2 - tx0;
        float texDy2 = ty2 - ty0;
        
        float screenDx1 = tri.x2 - tri.x1;
        float screenDy1 = tri.y2 - tri.y1;
        float screenDx2 = tri.x3 - tri.x1;
        float screenDy2 = tri.y3 - tri.y1;
        
        // Solve for transform matrix [a c e] that maps texture to screen
        //                    [b d f]
        // [screenDx1 screenDx2] = [texDx1 texDx2] * [a c]
        // [screenDy1 screenDy2]   [texDy1 texDy2]   [b d]
        
        float det = texDx1 * texDy2 - texDx2 * texDy1;
        if (Math.abs(det) < 0.0001f) {
            // Degenerate triangle, fall back to solid color
            g2d.setClip(oldClip);
            return;
        }
        
        float a = (screenDx1 * texDy2 - screenDx2 * texDy1) / det;
        float b = (screenDy1 * texDy2 - screenDy2 * texDy1) / det;
        float c = (screenDx2 * texDx1 - screenDx1 * texDx2) / det;
        float d = (screenDy2 * texDx1 - screenDy1 * texDx2) / det;
        
        // Calculate translation: e = x1 - (a*tx0 + c*ty0), f = y1 - (b*tx0 + d*ty0)
        float e = tri.x1 - (a * tx0 + c * ty0);
        float f = tri.y1 - (b * tx0 + d * ty0);
        
        // Create and apply transform
        AffineTransform transform = new AffineTransform(a, b, c, d, e, f);
        AffineTransform oldTransform = g2d.getTransform();
        g2d.transform(transform);
        
        // Draw the texture (will be clipped to triangle shape)
        g2d.drawImage(texture, 0, 0, null);
        
        // Restore
        g2d.setTransform(oldTransform);
        g2d.setClip(oldClip);
    }
    
    /**
     * Simple 2D triangle with depth and UV coordinates for sorting.
     */
    private static class Triangle2D {
        int x1, y1, x2, y2, x3, y3;
        float depth;
        float u0, v0, u1, v1, u2, v2;
        
        Triangle2D(int x1, int y1, int x2, int y2, int x3, int y3, float depth,
                   float u0, float v0, float u1, float v1, float u2, float v2) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.x3 = x3; this.y3 = y3;
            this.depth = depth;
            this.u0 = u0; this.v0 = v0;
            this.u1 = u1; this.v1 = v1;
            this.u2 = u2; this.v2 = v2;
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
