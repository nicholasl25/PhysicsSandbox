package tensor;

import java.util.ArrayList;

/** Rank-0 tensor: single component with {@code dim == 0} and empty index list. */
public class Scalar extends Tensor {

    public Scalar(String name, double value) {
        super(name, scalarData(value), new Index[0], 0);
    }

    public Scalar(double value) {
        this("scalar", value);
    }

    private static ArrayList<Double> scalarData(double value) {
        ArrayList<Double> data = new ArrayList<>(1);
        data.add(value);
        return data;
    }

    public double getValue() {
        return getdata().get(0);
    }

    public void setValue(double value) {
        getdata().set(0, value);
    }

    /**
     * Sum of two dimensionless scalars. Returns a {@link Scalar} (not a general {@link Tensor}) so
     * callers can use {@link #getValue()} without narrowing a rank-0 {@link Tensor}.
     */
    public Scalar add(Scalar other) {
        return new Scalar(this.name, this.getValue() + other.getValue());
    }

    /**
     * {@inheritDoc}
     * <p>
     * When the right-hand side is another {@link Scalar}, the product is still rank-0; the base
     * {@link Tensor#mulTensor(Tensor)} would return a plain {@link Tensor}. This override returns a
     * {@link Scalar} for the same reason as {@link #add(Scalar)}.
     * <p>
     * When the right-hand side is a {@link Vector}, the mathematical result is still a rank-1 tensor
     * with the same index as that vector; the base implementation would return a plain {@link Tensor}.
     * This override narrows that result to {@link Vector} so callers keep vector-specific API
     * (e.g. {@link Vector#getIndex()}, norms) without an explicit cast.
     */
    @Override
    public Tensor mulTensor(Tensor other) {
        if (other instanceof Scalar) {
            Scalar s = (Scalar) other;
            return new Scalar(this.name, this.getValue() * s.getValue());
        }
        if (other instanceof Vector) {
            Vector v = (Vector) other;
            Tensor raw = super.mulTensor(v);
            return new Vector(v.name, raw.getdata(), raw.getIndices()[0], raw.getDim());
        }
        return super.mulTensor(other);
    }
}
