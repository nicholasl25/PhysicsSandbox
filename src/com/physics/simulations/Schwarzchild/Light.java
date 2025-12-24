package com.physics.simulations.Schwarzchild;

public class Light {

    double G = 1.0;
    double M = 1.0;
    
    double[] threeVel;
    double[] threePos;

    public Light(double r, double theta, double vr, double vtheta) {
        this.threePos = new double[] {0.0, r, theta};

        double f = 1 - 2 * G * M / (r * 2);
        double vt = Math.sqrt((vr * vr / f * f) + (r * r * vtheta * vtheta));

        this.threePos = new double[] {vt, vr, vtheta};
    }

    public double[][] getMetric() {
        double[][] metric = new double[3][3];
        double r = this.threePos[1];

        double f = 1 - 2 * G * M / (r * 2);

        /* Standard Schwarzchild Metric */
        metric[0][0] = f;
        metric[1][1] = 1 / f;
        metric[2][2] = r * r;

        return metric;
    }

    public double[][][] getChristoffel() {
        double[][][] chris = new double[3][3][3];
        double r = threePos[1];

        double f = 1 - 2 * G * M / (r * 2);


        /* Standard Christoffel Connection for Schwarzchild Metric */
        chris[0][0][1] = M / (f * r * r);
        chris[0][1][0] = M / (f * r * r);
        chris[1][0][0] = (M * f) /  (r * r);
        chris[1][1][1] = - M / (f * r * r);
        chris[1][2][2] = - f * r;
        chris[1][2][1] = 1/r;
        chris[1][1][2] = 1/r;

        return chris;
    }

    public void updateVel(double timeFactor) {
        double[][][] chris = this.getChristoffel();

        double t_acc = 0;
        double r_acc = 0;
        double theta_acc = 0;

        /* Replace with tensor multiplication method eventually */
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                t_acc += chris[0][i][j] * threeVel[i] * threeVel[j];
                r_acc += chris[1][i][j] * threeVel[i] * threeVel[j];
                theta_acc += chris[2][i][j] * threeVel[i] * threeVel[j];
            }
        }

        threeVel[0] += timeFactor * t_acc;
        threeVel[1] += timeFactor * r_acc;
        threeVel[2] += timeFactor * theta_acc;
    }

    public void updatePos(double timeFactor) {
        threePos[0] = threeVel[0] * timeFactor;
        threePos[1] = threeVel[1] * timeFactor;
        threePos[2] = threeVel[2] * timeFactor;
    }

    public double norm() {
        double[][] metric = this.getMetric();

        double norm = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                norm += metric[i][j] * threeVel[i] * threeVel[j];
            }
        }

        return norm;
    }

    

}