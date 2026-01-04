package simulations.NewtonianGravity;

public class Matrix {
    private double[] data;

    public Matrix(double[] data) {
        this.data = data;
    }

    static Matrix identity(int n) {
        double[] data = new double[n * n];
        for (int i = 0; i < n; i++) {
            data[i * n + i] = 1.0;
        }
        return new Matrix(data);
    }
}
