package simulations.NewtonianGravity.Gravity3D;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    
    private Vector3f position;
    private float yaw;
    private float pitch;
    
    private float fov = (float) Math.toRadians(60.0);
    private float nearPlane = 0.1f;
    private float farPlane = 10000.0f;
    
    private Vector3f floatingOrigin;
    private static final float FLOATING_ORIGIN_THRESHOLD = 1000.0f;
    
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;
    private boolean viewMatrixDirty = true;
    private boolean projectionMatrixDirty = true;
    
    private int width = 800;
    private int height = 600;
    
    // Floating origin prevents precision loss at large distances
    
    public Camera() {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.yaw = 0.0f;
        this.pitch = 0.0f;
        this.floatingOrigin = new Vector3f(0.0f, 0.0f, 0.0f);
        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
    }
    
    public Camera(float x, float y, float z) {
        this();
        this.position.set(x, y, z);
        this.viewMatrixDirty = true;
    }
    
    public Vector3f getPosition() {
        return new Vector3f(position);
    }
    
    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        this.viewMatrixDirty = true;
        updateFloatingOrigin();
    }
    
    public void setPosition(Vector3f pos) {
        this.position.set(pos);
        this.viewMatrixDirty = true;
        updateFloatingOrigin();
    }
    
    public void move(float forward, float right, float up) {
        float forwardX = (float) (Math.sin(yaw) * forward);
        float forwardZ = (float) (-Math.cos(yaw) * forward);
        float rightX = (float) (Math.cos(yaw) * right);
        float rightZ = (float) (Math.sin(yaw) * right);
        
        position.add(forwardX + rightX, up, forwardZ + rightZ);
        this.viewMatrixDirty = true;
        updateFloatingOrigin();
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public void setYaw(float yaw) {
        this.yaw = yaw;
        // Normalize to [-PI, PI]
        while (this.yaw > Math.PI) this.yaw -= 2 * Math.PI;
        while (this.yaw < -Math.PI) this.yaw += 2 * Math.PI;
        this.viewMatrixDirty = true;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public void setPitch(float pitch) {
        this.pitch = pitch;
        // Clamp to prevent gimbal lock
        float maxPitch = (float) (Math.PI / 2.0 - 0.1);
        if (this.pitch > maxPitch) this.pitch = maxPitch;
        if (this.pitch < -maxPitch) this.pitch = -maxPitch;
        this.viewMatrixDirty = true;
    }
    
    public void rotate(float deltaYaw, float deltaPitch) {
        setYaw(yaw + deltaYaw);
        setPitch(pitch + deltaPitch);
    }
    
    public void setViewport(int width, int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            this.projectionMatrixDirty = true;
        }
    }
    
    public Matrix4f getViewMatrix() {
        if (viewMatrixDirty) {
            updateViewMatrix();
            viewMatrixDirty = false;
        }
        return new Matrix4f(viewMatrix);
    }
    
    public Matrix4f getProjectionMatrix() {
        if (projectionMatrixDirty) {
            updateProjectionMatrix();
            projectionMatrixDirty = false;
        }
        return new Matrix4f(projectionMatrix);
    }
    
    private void updateViewMatrix() {
        float cosPitch = (float) Math.cos(pitch);
        float sinPitch = (float) Math.sin(pitch);
        float cosYaw = (float) Math.cos(yaw);
        float sinYaw = (float) Math.sin(yaw);
        
        Vector3f direction = new Vector3f(
            cosPitch * sinYaw,
            sinPitch,
            -cosPitch * cosYaw
        );
        
        Vector3f right = new Vector3f(direction).cross(0, 1, 0).normalize();
        Vector3f up = new Vector3f(right).cross(direction).normalize();
        Vector3f cameraPos = new Vector3f(position).sub(floatingOrigin);
        
        viewMatrix.identity();
        viewMatrix.lookAt(cameraPos, new Vector3f(cameraPos).add(direction), up);
    }
    
    private void updateProjectionMatrix() {
        float aspect = (float) width / (float) height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspect, nearPlane, farPlane);
    }
    
    // Updates floating origin when camera moves far from origin
    private void updateFloatingOrigin() {
        if (position.length() > FLOATING_ORIGIN_THRESHOLD) {
            floatingOrigin.set(position);
            position.set(0, 0, 0);
        }
    }
    
    public Vector3f getFloatingOrigin() {
        return new Vector3f(floatingOrigin);
    }
    
    public float getFOV() {
        return fov;
    }
    
    public void setFOV(float fovRadians) {
        this.fov = fovRadians;
        this.projectionMatrixDirty = true;
    }
    
    public void setPlanes(float near, float far) {
        this.nearPlane = near;
        this.farPlane = far;
        this.projectionMatrixDirty = true;
    }
}
