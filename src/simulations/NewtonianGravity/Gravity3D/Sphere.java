package simulations.NewtonianGravity.Gravity3D;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.jogamp.opengl.GL3;
import com.jogamp.common.nio.Buffers;

public class Sphere {
    
    // Static mesh data - shared by all instances
    private static int vao;
    private static int vbo;
    private static int ebo;
    private static int indexCount;
    private static boolean meshInitialized = false;
    private static int shaderProgram;
    private static GL3 currentGL; // Store current GL context

    public Sphere() {

    }

    public static void initializeMesh(GL3 gl, int segments, int rings) {
        if (meshInitialized) {
            return;
        }
        
        currentGL = gl;

        Sphere temp = new Sphere();
        float[] vertices = temp.generateVertices(segments, rings);
        int[] indices = temp.generateIndices(segments, rings);
        indexCount = indices.length;

        // Generate IDs
        int[] vaos = new int[1];
        int[] vbos = new int[1];
        int[] ebos = new int[1];
        gl.glGenVertexArrays(1, vaos, 0);
        gl.glGenBuffers(1, vbos, 0);
        gl.glGenBuffers(1, ebos, 0);
        vao = vaos[0];
        vbo = vbos[0];
        ebo = ebos[0];

        gl.glBindVertexArray(vao);

        // Upload vertex data to VBO
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(vertices);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexBuffer.limit() * 4, vertexBuffer, GL3.GL_STATIC_DRAW);

        // Upload index data to EBO
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, ebo);
        IntBuffer indexBuffer = Buffers.newDirectIntBuffer(indices);
        gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.limit() * 4, indexBuffer, GL3.GL_STATIC_DRAW);

        // Set vertex attribute pointers (stride = 8 floats, 32 bytes)
        // Position (location 0): offset 0
        gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, 8 * 4, 0);
        gl.glEnableVertexAttribArray(0);

        // Normal (location 1): offset 12 bytes
        gl.glVertexAttribPointer(1, 3, GL3.GL_FLOAT, false, 8 * 4, 3 * 4);
        gl.glEnableVertexAttribArray(1);

        // TexCoord (location 2): offset 24 bytes
        gl.glVertexAttribPointer(2, 2, GL3.GL_FLOAT, false, 8 * 4, 6 * 4);
        gl.glEnableVertexAttribArray(2);

        gl.glBindVertexArray(0);

        shaderProgram = createShaderProgram(gl, "resources/shaders/sphere_vertex.glsl", "resources/shaders/sphere_fragment.glsl");
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

    public static void render(GL3 gl, Matrix4f modelMatrix, Matrix4f viewMatrix, Matrix4f projectionMatrix, Vector3f color) {
        gl.glUseProgram(shaderProgram);

        int modelLoc = gl.glGetUniformLocation(shaderProgram, "model");
        int viewLoc = gl.glGetUniformLocation(shaderProgram, "view");
        int projLoc = gl.glGetUniformLocation(shaderProgram, "projection");
        int colorLoc = gl.glGetUniformLocation(shaderProgram, "color");

        FloatBuffer modelBuffer = Buffers.newDirectFloatBuffer(16);
        modelMatrix.get(modelBuffer);
        gl.glUniformMatrix4fv(modelLoc, 1, false, modelBuffer);

        FloatBuffer viewBuffer = Buffers.newDirectFloatBuffer(16);
        viewMatrix.get(viewBuffer);
        gl.glUniformMatrix4fv(viewLoc, 1, false, viewBuffer);

        FloatBuffer projBuffer = Buffers.newDirectFloatBuffer(16);
        projectionMatrix.get(projBuffer);
        gl.glUniformMatrix4fv(projLoc, 1, false, projBuffer);

        gl.glUniform3f(colorLoc, color.x, color.y, color.z);

        gl.glBindVertexArray(vao);
        gl.glDrawElements(GL3.GL_TRIANGLES, indexCount, GL3.GL_UNSIGNED_INT, 0);
        gl.glBindVertexArray(0);
    }

    public static void cleanupStatic(GL3 gl) {
        if (meshInitialized) {
            int[] vaos = {vao};
            int[] vbos = {vbo};
            int[] ebos = {ebo};
            gl.glDeleteVertexArrays(1, vaos, 0);
            gl.glDeleteBuffers(1, vbos, 0);
            gl.glDeleteBuffers(1, ebos, 0);
            gl.glDeleteProgram(shaderProgram);
            meshInitialized = false;
        }
    }

    private static int loadShader(GL3 gl, String filePath, int shaderType) {
        try {
            String source = new String(Files.readAllBytes(new File(filePath).toPath()));
            int shader = gl.glCreateShader(shaderType);
            
            String[] sources = {source};
            int[] lengths = {source.length()};
            gl.glShaderSource(shader, 1, sources, lengths, 0);
            gl.glCompileShader(shader);

            int[] compileStatus = new int[1];
            gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == GL3.GL_FALSE) {
                int[] logLength = new int[1];
                gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
                if (logLength[0] > 0) {
                    byte[] logBytes = new byte[logLength[0]];
                    gl.glGetShaderInfoLog(shader, logLength[0], null, 0, logBytes, 0);
                    String log = new String(logBytes);
                    System.err.println("Shader compilation error: " + log);
                }
                gl.glDeleteShader(shader);
                return -1;
            }
            return shader;
        } catch (IOException e) {
            System.err.println("Failed to load shader: " + filePath);
            return -1;
        }
    }

    private static int createShaderProgram(GL3 gl, String vertexPath, String fragmentPath) {
        int vertexShader = loadShader(gl, vertexPath, GL3.GL_VERTEX_SHADER);
        int fragmentShader = loadShader(gl, fragmentPath, GL3.GL_FRAGMENT_SHADER);

        if (vertexShader == -1 || fragmentShader == -1) {
            return -1;
        }

        int program = gl.glCreateProgram();
        gl.glAttachShader(program, vertexShader);
        gl.glAttachShader(program, fragmentShader);
        gl.glLinkProgram(program);

        int[] linkStatus = new int[1];
        gl.glGetProgramiv(program, GL3.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GL3.GL_FALSE) {
            int[] logLength = new int[1];
            gl.glGetProgramiv(program, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
            if (logLength[0] > 0) {
                byte[] logBytes = new byte[logLength[0]];
                gl.glGetProgramInfoLog(program, logLength[0], null, 0, logBytes, 0);
                String log = new String(logBytes);
                System.err.println("Shader program linking error: " + log);
            }
            gl.glDeleteProgram(program);
            gl.glDeleteShader(vertexShader);
            gl.glDeleteShader(fragmentShader);
            return -1;
        }

        gl.glDeleteShader(vertexShader);
        gl.glDeleteShader(fragmentShader);

        return program;
    }
}
