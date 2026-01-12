package simulations.NewtonianGravity;

/**
 * Vector class - represents a mathematical vector as an array.
 * Supports common vector operations needed for physics simulations.
 */
public class Vector {
    private double[] data;
    
    /**
     * Creates a vector from an array of components.
     */
    public Vector(double[] components) {
        this.data = components.clone();
    }
    
    /**
     * Creates a vector with specified number of dimensions, initialized to zero.
     */
    public Vector(int dimensions) {
        this.data = new double[dimensions];
    }
    
    
    /**
     * Gets the number of dimensions.
     */
    public int dimensions() {
        return data.length;
    }
    
    /**
     * Gets a component by index.
     */
    public double get(int index) {
        return data[index];
    }
    
    /**
     * Sets a component by index.
     */
    public void set(int index, double value) {
        data[index] = value;
    }
    
    /**
     * Gets the underlying array (defensive copy).
     */
    public double[] getData() {
        return data.clone();
    }
    
    /**
     * Adds another vector to this vector (element-wise).
     */
    public Vector add(Vector other) {
        if (this.data.length != other.data.length) {
            throw new IllegalArgumentException("Vectors must have same dimensions");
        }
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = this.data[i] + other.data[i];
        }
        return new Vector(result);
    }
    
    /**
     * Subtracts another vector from this vector.
     */
    public Vector subtract(Vector other) {
        if (this.data.length != other.data.length) {
            throw new IllegalArgumentException("Vectors must have same dimensions");
        }
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = this.data[i] - other.data[i];
        }
        return new Vector(result);
    }
    
    /**
     * Multiplies this vector by a scalar.
     */
    public Vector multiply(double scalar) {
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = this.data[i] * scalar;
        }
        return new Vector(result);
    }


    public Vector divide(double scalar) {
        return multiply(1.0 / scalar);
    }
    /**
     * Computes the dot product with another vector.
     */
    public double dot(Vector other) {
        if (this.data.length != other.data.length) {
            throw new IllegalArgumentException("Vectors must have same dimensions");
        }
        double result = 0.0;
        for (int i = 0; i < data.length; i++) {
            result += this.data[i] * other.data[i];
        }
        return result;
    }

    public Vector cross(Vector other) {
        if (this.data.length != 3 || other.data.length != 3) {
            throw new IllegalArgumentException("Vectors must be 3D to take the cross product");
        }
        double[] result = new double[3];
        result[0] = this.data[1] * other.data[2] - this.data[2] * other.data[1];
        result[1] = this.data[2] * other.data[0] - this.data[0] * other.data[2];
        result[2] = this.data[0] * other.data[1] - this.data[1] * other.data[0];
        return new Vector(result);
    }
    
    /**
     * Computes the magnitude (length) of the vector.
     */
    public double magnitude() {
        double sumSquares = 0.0;
        for (double d : data) {
            sumSquares += d * d;
        }
        return Math.sqrt(sumSquares);
    }
    
    /**
     * Returns a normalized (unit) vector.
     */
    public Vector normalize() {
        double mag = magnitude();
        if (mag == 0.0) {
            throw new ArithmeticException("Cannot normalize zero vector");
        }
        return multiply(1.0 / mag);
    }
    
    /**
     * Creates a copy of this vector.
     */
    public Vector clone() {
        return new Vector(this.data);
    }
    
    /**
     * Returns a string representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < data.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(data[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}

