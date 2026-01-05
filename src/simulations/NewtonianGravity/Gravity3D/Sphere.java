package simulations.NewtonianGravity.Gravity3D;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Sphere {
    
    // Static mesh data - shared by all instances
    private static int vao;
    private static int vbo;
    private static int ebo;
    private static int indexCount;
    private static boolean meshInitialized = false;
    private static int shaderProgram;

    public Sphere() {

    }

    public static void initializeMesh(int segments, int rings) {
        if (meshInitialized) {
            return;
        }

        Sphere temp = new Sphere();
        float[] vertices = temp.generateVertices(segments, rings);
        int[] indices = temp.generateIndices(segments, rings);
        indexCount = indices.length;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length).put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * 4, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * 4, 3 * 4);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * 4, 6 * 4);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);

        shaderProgram = createShaderProgram("resources/shaders/sphere_vertex.glsl", "resources/shaders/sphere_fragment.glsl");
        if (shaderProgram == -1) {
            throw new RuntimeException("Failed to create shader program");
        }

        meshInitialized = true;
    }

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

    public int[] generateIndices(int segments, int rings){
        int face_count = 6 * (segments) * (rings);
        int[] answer = new int[face_count];

        int topLeft, topRight, bottomLeft, bottomRight;
        int idx = 0;
        for(int i = 0; i < rings; i++) {
            for(int j = 0; j < segments; j++) {
                topLeft = i * (segments + 1) + j;
                topRight = i * (segments + 1) + j + 1;
                bottomLeft = (i + 1) * (segments + 1) + j;
                bottomRight = (i + 1) * (segments + 1) + j + 1;

                // Divide face into 2 triangles (TL, TR, BL) and (TR, BR, BL)
                answer[idx * 6] = topLeft;
                answer[idx * 6 + 1] = topRight;
                answer[idx * 6 + 2] = bottomLeft;

                answer[idx * 6 + 3] = topRight;
                answer[idx * 6 + 4] = bottomRight;
                answer[idx * 6 + 5] = bottomLeft;

                idx += 1;
            }
        }

        return answer;
    }

    public static void render(Matrix4f modelMatrix, Matrix4f viewMatrix, Matrix4f projectionMatrix, Vector3f color) {
        glUseProgram(shaderProgram);

        int modelLoc = glGetUniformLocation(shaderProgram, "model");
        int viewLoc = glGetUniformLocation(shaderProgram, "view");
        int projLoc = glGetUniformLocation(shaderProgram, "projection");
        int colorLoc = glGetUniformLocation(shaderProgram, "color");

        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        modelMatrix.get(modelBuffer);
        glUniformMatrix4fv(modelLoc, false, modelBuffer);

        FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
        viewMatrix.get(viewBuffer);
        glUniformMatrix4fv(viewLoc, false, viewBuffer);

        FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
        projectionMatrix.get(projBuffer);
        glUniformMatrix4fv(projLoc, false, projBuffer);

        glUniform3f(colorLoc, color.x, color.y, color.z);

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public static void cleanupStatic() {
        if (meshInitialized) {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            glDeleteBuffers(ebo);
            glDeleteProgram(shaderProgram);
            meshInitialized = false;
        }
    }

    private static int loadShader(String filePath, int shaderType) {
        try {
            String source = new String(Files.readAllBytes(new File(filePath).toPath()));
            int shader = glCreateShader(shaderType);
            glShaderSource(shader, source);
            glCompileShader(shader);

            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
                String log = glGetShaderInfoLog(shader);
                System.err.println("Shader compilation error: " + log);
                glDeleteShader(shader);
                return -1;
            }
            return shader;
        } catch (IOException e) {
            System.err.println("Failed to load shader: " + filePath);
            return -1;
        }
    }

    private static int createShaderProgram(String vertexPath, String fragmentPath) {
        int vertexShader = loadShader(vertexPath, GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentPath, GL_FRAGMENT_SHADER);

        if (vertexShader == -1 || fragmentShader == -1) {
            return -1;
        }

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            System.err.println("Shader program linking error: " + log);
            glDeleteProgram(program);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            return -1;
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }
}
