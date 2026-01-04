package simulations.NewtonianGravity.Gravity3D;

public class Gravity3DTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Gravity3D classes...\n");
        
        testGenerateVertices();
        
        System.out.println("\nAll tests completed!");
    }
    
    public static void testGenerateVertices() {
        Sphere sphere = new Sphere();
        float[] vertices = sphere.generateVertices(4, 2);
        
        // Test array size
        int expectedSize = (4 + 1) * (2 + 1) * 8;
        if (vertices.length != expectedSize) {
            System.out.println("FAIL: generateVertices() - Array size mismatch");
            return;
        }
        
        // Test coords on a given ring are equally spaces
        float x = vertices[0], y = vertices[1], z = vertices[2];
        float nx = vertices[3], ny = vertices[4], nz = vertices[5];
        if (Math.abs(x - nx) > 0.001f || Math.abs(y - ny) > 0.001f || Math.abs(z - nz) > 0.001f) {
            System.out.println("FAIL: generateVertices() - Normal != position");
            return;
        }
        
        // Test texture coordinates
        float u = vertices[6], v = vertices[7];
        if (u < 0 || u > 1 || v < 0 || v > 1) {
            System.out.println("FAIL: generateVertices() - Texture coords out of range");
            return;
        }
        
        // Test vertices are unit length
        for (int i = 0; i < vertices.length; i += 8) {
            float dist = (float) Math.sqrt(vertices[i]*vertices[i] + vertices[i+1]*vertices[i+1] + vertices[i+2]*vertices[i+2]);
            if (Math.abs(dist - 1.0f) > 0.01f) {
                System.out.println("FAIL: generateVertices() - Vertex not unit length at index " + i);
                return;
            }
        }
        
        System.out.println("PASS: generateVertices()");
    }
}

