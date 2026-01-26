/**
 * Vector class - represents a mathematical vector as an array.
 * Supports common vector operations needed for physics simulations.
 */
class Vector {
    constructor(components) {
        if (Array.isArray(components)) {
            this.data = [...components];
        } else if (typeof components === 'number') {
            // Create zero vector with specified dimensions
            this.data = new Array(components).fill(0);
        } else {
            throw new Error('Vector must be initialized with array or dimension count');
        }
    }
    
    /**
     * Gets the number of dimensions.
     */
    dimensions() {
        return this.data.length;
    }
    
    /**
     * Gets a component by index.
     */
    get(index) {
        return this.data[index];
    }
    
    /**
     * Sets a component by index.
     */
    set(index, value) {
        this.data[index] = value;
    }
    
    /**
     * Gets the underlying array (defensive copy).
     */
    getData() {
        return [...this.data];
    }
    
    /**
     * Adds another vector to this vector (element-wise).
     */
    add(other) {
        if (this.data.length !== other.data.length) {
            throw new Error('Vectors must have same dimensions');
        }
        const result = new Array(this.data.length);
        for (let i = 0; i < this.data.length; i++) {
            result[i] = this.data[i] + other.data[i];
        }
        return new Vector(result);
    }
    
    /**
     * Subtracts another vector from this vector.
     */
    subtract(other) {
        if (this.data.length !== other.data.length) {
            throw new Error('Vectors must have same dimensions');
        }
        const result = new Array(this.data.length);
        for (let i = 0; i < this.data.length; i++) {
            result[i] = this.data[i] - other.data[i];
        }
        return new Vector(result);
    }
    
    /**
     * Multiplies this vector by a scalar.
     */
    multiply(scalar) {
        const result = new Array(this.data.length);
        for (let i = 0; i < this.data.length; i++) {
            result[i] = this.data[i] * scalar;
        }
        return new Vector(result);
    }
    
    /**
     * Divides this vector by a scalar.
     */
    divide(scalar) {
        return this.multiply(1.0 / scalar);
    }
    
    /**
     * Computes the dot product with another vector.
     */
    dot(other) {
        if (this.data.length !== other.data.length) {
            throw new Error('Vectors must have same dimensions');
        }
        let result = 0.0;
        for (let i = 0; i < this.data.length; i++) {
            result += this.data[i] * other.data[i];
        }
        return result;
    }
    
    /**
     * Computes the cross product with another vector (3D only).
     */
    cross(other) {
        if (this.data.length !== 3 || other.data.length !== 3) {
            throw new Error('Vectors must be 3D to take the cross product');
        }
        return new Vector([
            this.data[1] * other.data[2] - this.data[2] * other.data[1],
            this.data[2] * other.data[0] - this.data[0] * other.data[2],
            this.data[0] * other.data[1] - this.data[1] * other.data[0]
        ]);
    }
    
    /**
     * Computes the magnitude (length) of the vector.
     */
    magnitude() {
        let sumSquares = 0.0;
        for (let d of this.data) {
            sumSquares += d * d;
        }
        return Math.sqrt(sumSquares);
    }
    
    /**
     * Returns a normalized (unit) vector.
     */
    normalize() {
        const mag = this.magnitude();
        if (mag === 0.0) {
            throw new Error('Cannot normalize zero vector');
        }
        return this.multiply(1.0 / mag);
    }
    
    /**
     * Adds another vector to this vector in-place.
     */
    addTo(other) {
        if (this.data.length !== other.data.length) {
            throw new Error('Vectors must have same dimensions');
        }
        for (let i = 0; i < this.data.length; i++) {
            this.data[i] += other.data[i];
        }
        return this;
    }
    
    /**
     * Creates a copy of this vector.
     */
    clone() {
        return new Vector(this.data);
    }
    
    /**
     * Converts to Three.js Vector3 (if 3D).
     */
    toThreeVector3() {
        if (this.data.length < 3) {
            throw new Error('Vector must be at least 3D to convert to Vector3');
        }
        return new THREE.Vector3(this.data[0], this.data[1], this.data[2]);
    }
    
    /**
     * Returns a string representation.
     */
    toString() {
        return '(' + this.data.join(', ') + ')';
    }
}
