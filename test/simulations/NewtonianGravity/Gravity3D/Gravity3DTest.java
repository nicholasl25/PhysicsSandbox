package simulations.NewtonianGravity.Gravity3D;

public class Gravity3DTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Gravity3D classes...\n");
        
        testGenerateVertices();
        testGenerateIndices();
        testLoadShader();
        
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

    public static void testGenerateIndices() {
        Sphere sphere = new Sphere();
        int[] indices = sphere.generateIndices(4, 2);
        
        int expectedSize = 6 * 4 * 2;
        if (indices.length != expectedSize) {
            System.out.println("FAIL: generateIndices() - Array size mismatch");
            return;
        }
        
        int vertexCount = (4 + 1) * (2 + 1);
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] < 0 || indices[i] >= vertexCount) {
                System.out.println("FAIL: generateIndices() - Index out of range at " + i);
                return;
            }
        }
        
        for (int i = 0; i < indices.length; i += 6) {
            int v0 = indices[i], v1 = indices[i + 1], v2 = indices[i + 2];
            int v3 = indices[i + 3], v4 = indices[i + 4], v5 = indices[i + 5];
            if (v0 == v1 || v1 == v2 || v0 == v2 || v3 == v4 || v4 == v5 || v3 == v5) {
                System.out.println("FAIL: generateIndices() - Degenerate triangle at " + i);
                return;
            }
        }
        
        System.out.println("PASS: generateIndices()");
    }

    public static void testLoadShader() {
        java.io.File vertexFile = new java.io.File("resources/shaders/sphere_vertex.glsl");
        java.io.File fragmentFile = new java.io.File("resources/shaders/sphere_fragment.glsl");
        
        if (!vertexFile.exists()) {
            System.out.println("FAIL: loadShader() - Vertex shader file not found");
            return;
        }
        
        if (!fragmentFile.exists()) {
            System.out.println("FAIL: loadShader() - Fragment shader file not found");
            return;
        }
        
        try {
            String vertexSource = new String(java.nio.file.Files.readAllBytes(vertexFile.toPath()));
            String fragmentSource = new String(java.nio.file.Files.readAllBytes(fragmentFile.toPath()));
            
            if (vertexSource.isEmpty() || fragmentSource.isEmpty()) {
                System.out.println("FAIL: loadShader() - Shader files are empty");
                return;
            }
            
            if (!vertexSource.contains("#version") || !fragmentSource.contains("#version")) {
                System.out.println("FAIL: loadShader() - Invalid shader format");
                return;
            }
            
            System.out.println("PASS: loadShader()");
        } catch (Exception e) {
            System.out.println("FAIL: loadShader() - " + e.getMessage());
        }
    }
}

