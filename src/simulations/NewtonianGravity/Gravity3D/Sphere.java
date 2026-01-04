package simulations.NewtonianGravity.Gravity3D;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class Sphere {
    
    // Static mesh data - shared by all instances
    private static int vao;
    private static int vbo;
    private static int ebo;
    private static int indexCount;
    private static boolean meshInitialized = false;

    public Sphere() {

    }

    /* 
    public static void generate_mesh(int segs, int rings) {
        if(meshInitialized) {
            return;
        }

        float[] vertices = generateVertices(segs, rings);
        int[] indices = generateIndices(segs, rings);
        indexCount = indices.length;


        // Generate IDs
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        // Bind VAO (records subsequent state)
        glBindVertexArray(vao);

        // Upload vertex data to VBO
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Upload index data to EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Set vertex attribute pointers (stride = 8 floats, 32 bytes)
        // Position (location 0): offset 0
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0);
        glEnableVertexAttribArray(0);

        // Normal (location 1): offset 12 bytes
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4);
        glEnableVertexAttribArray(1);

        // TexCoord (location 2): offset 24 bytes
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4);
        glEnableVertexAttribArray(2);

        // Unbind
        glBindVertexArray(0);

        meshInitialized = true;
    }
    */

    public float[] generateVertices(int segments, int rings){
        int vertex_count = (segments + 1) * (rings + 1);
        float[] answer = new float[vertex_count * 8];

        double phi, theta;
        float x, y, z;
        int idx = 0;
        for(int i = 0; i <= rings; i++) {
            for(int j = 0; j <= segments; j++) {
                // Divide rings and segments evenly
                phi = Math.PI * ((double)i / rings);
                theta = 2 * Math.PI * ((double)j / segments);

                // Polar to Cartesian (r = 1)
                x = (float)(Math.sin(phi) * Math.cos(theta));
                y = (float)(Math.cos(phi));
                z = (float)(Math.sin(phi) * Math.sin(theta));

                // Normal Vector = Position Vector for unit sphere
                answer[idx * 8] = x;
                answer[idx * 8 + 1] = y;
                answer[idx * 8 + 2] = z;
                answer[idx * 8 + 3] = x;
                answer[idx * 8 + 4] = y;
                answer[idx * 8 + 5] = z;

                // Texture coords [0, 1]
                answer[idx * 8 + 6] = (float)i / rings;
                answer[idx * 8 + 7] = (float)j / segments;

                idx += 1;
            }
        }
        return answer;
    }


}
