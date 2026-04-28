package tensor;

import java.util.ArrayList;

/**
 * Rank-1 tensor: one {@link Index} and {@code data.size() == dim}. Constructors take a single index,
 * not an array (internally stored as a one-element {@code Index[]}).
 */
public class Vector extends Tensor {

    public Vector(String name, ArrayList<Double> data, Index index, int dim) {
        super(name, data, new Index[] { index }, dim);
        if (dim <= 0) {
            throw new TensorConsistencyException("Vector dimension must be positive");
        }
    }

    public Vector(String name, double[] components, Index index, int dim) {
        this(name, toList(components), index, dim);
    }


    private static ArrayList<Double> toList(double[] components) {
        ArrayList<Double> list = new ArrayList<>(components.length);
        for (double x : components) {
            list.add(x);
        }
        return list;
    }

    /** The sole tensor index for this vector. */
    public Index getIndex() {
        return getIndices()[0];
    }

    /** Number of components ({@code dim} for a valid vector). */
    public int length() {
        return getdata().size();
    }

    /** {@code √(Σᵢ xᵢ²)} */
    public double norm2() {
        double sum = 0.0;
        for (double x : getdata()) {
            sum += x * x;
        }
        return Math.sqrt(sum);
    }

    /** {@code Σᵢ |xᵢ|} */
    public double norm1() {
        double sum = 0.0;
        for (double x : getdata()) {
            sum += Math.abs(x);
        }
        return sum;
    }

    /** {@code maxᵢ |xᵢ|} */
    public double normInf() {
        double m = 0.0;
        for (double x : getdata()) {
            m = Math.max(m, Math.abs(x));
        }
        return m;
    }

    @Override
    public String toString() {
        return super.toString().replaceFirst("^Tensor:", "Vector:");
    }
}
