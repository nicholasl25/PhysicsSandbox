package simulations.tensor;

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
}
