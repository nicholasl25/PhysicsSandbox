package com.physics.simulations.Schwarzchild;

import java.util.ArrayList;

public class Light {

    double[] threeVel;
    double[] threePos;
    
    private ArrayList<double[]> trajectory;
    private SchwarzchildSimulation simulation;

    public Light(double r, double theta, double vr, double vtheta, SchwarzchildSimulation simulation) {
        this.simulation = simulation;
        this.threePos = new double[] {0.0, r, theta};

        double f = 1 - 2 * simulation.G * simulation.M / r;
        double spatialVelMagnitude = Math.sqrt(vr * vr + (r * vtheta) * (r * vtheta));
        double targetMagnitude = Math.sqrt(1 - 2 * simulation.G * simulation.M / r);
        
        if (spatialVelMagnitude > 0) {
            double scale = targetMagnitude / spatialVelMagnitude;
            vr *= scale;
            vtheta *= scale;
        }
        
        double vt = Math.sqrt((vr * vr / (f * f)) + (r * r * vtheta * vtheta));

        this.threeVel = new double[] {vt, vr, vtheta};
        this.trajectory = new ArrayList<>();
    }

    public void updateVel(double timeFactor) {
        double r = threePos[1];
        double[][][] chris = simulation.getChristoffel(r);

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
        threePos[0] += threeVel[0] * timeFactor;
        threePos[1] += threeVel[1] * timeFactor;
        threePos[2] += threeVel[2] * timeFactor;
        
        

        double[] cartPos = getCartesianPos();
        trajectory.add(new double[] {cartPos[1], cartPos[2]});
        
        if (trajectory.size() > 10000) {
            trajectory.remove(0);
        }
    }
    
    public ArrayList<double[]> getTrajectory() {
        return trajectory;
    }

    public double norm() {
        double r = threePos[1];
        double[][] metric = simulation.getMetric(r);

        double norm = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                norm += metric[i][j] * threeVel[i] * threeVel[j];
            }
        }

        return norm;
    }

    public double[] getCartesianVel() {
        double vt = threeVel[0];
        double r = threePos[1];
        double theta = threePos[2];

        double vx = threeVel[1] * Math.cos(theta) - r * threeVel[2] * Math.sin(theta);
        double vy = threeVel[1] * Math.sin(theta) + r * threeVel[2] * Math.cos(theta);

        return new double[] {vt, vx, vy};
    }

    public double[] getCartesianPos() {
        double t = threePos[0];
        double r = threePos[1];
        double theta = threePos[2];

        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);

        return new double[] {t, x, y};
    }

    public boolean isOutsideEventHorizon() {
        return threePos[1] > simulation.getSchwarzchildRadius();
    }


}