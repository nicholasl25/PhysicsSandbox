package com.physics.simulations.Schwarzchild;

import com.physics.simulations.BaseSimulation;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SchwarzchildSimulation extends BaseSimulation {
    
    private Timer animationTimer;
    private static final double DELTA_TIME = 1.0 / 60.0;
    
    private DrawingPanel drawingPanel;
    
    private ArrayList<Light> lightRays;
    
    private boolean isPaused = false;
    
    public double G = 1.0;
    public double M = 1.0;
    
    // ============================================================================
    // SCHWARZSCHILD-SPECIFIC METHODS
    // These methods are specific to the Schwarzschild metric implementation.
    // For a generic GR simulation, these would be abstract or implemented differently.
    // ============================================================================
    
    public double[][] getMetric(double r) {
        double[][] metric = new double[3][3];
        double f = 1 - 2 * G * M / (r * r);
        
        metric[0][0] = f;
        metric[1][1] = 1 / f;
        metric[2][2] = r * r;
        
        return metric;
    }
    
    public double[][][] getChristoffel(double r) {
        double[][][] chris = new double[3][3][3];
        double f = 1 - 2 * G * M / (r * r);
        
        chris[0][0][1] = M / (f * r * r);
        chris[0][1][0] = M / (f * r * r);
        chris[1][0][0] = (M * f) / (r * r);
        chris[1][1][1] = -M / (f * r * r);
        chris[1][2][2] = -f * r;
        chris[1][2][1] = 1/r;
        chris[1][1][2] = 1/r;
        
        return chris;
    }
    
    public double getSchwarzchildRadius() {
        return 2 * G * M;
    }
    
    // ============================================================================
    // GENERIC GR SIMULATION METHODS
    // The methods below are generic and would work for any GR simulation.
    // ============================================================================
    
    @Override
    public void initialize() {
        setTitle("Schwarzschild Black Hole Simulation");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        lightRays = new ArrayList<>();
        
        double startX = -8.0;
        double[] startYs = {-3.0, -1.5, 0.0, 1.5, 3.0};
        double[] angles = {-0.2, -0.1, 0.0, 0.1, 0.2};
        
        for (int i = 0; i < startYs.length; i++) {
            double startY = startYs[i];
            
            double r = Math.sqrt(startX * startX + startY * startY);
            double theta = Math.atan2(startY, startX);
            
            double directionAngle = angles[i];
            double spatialVelMagnitude = Math.sqrt(1 - 2 * G * M / r);
            
            // Ingoing radial null geodesic (vr < 0)
            double vr = -1 * spatialVelMagnitude * Math.cos(directionAngle);
            double vtheta = spatialVelMagnitude * Math.sin(directionAngle) / r;
            
            Light light = new Light(r, theta, vr, vtheta, this);
            lightRays.add(light);
        }
        
        drawingPanel = new DrawingPanel();
        add(drawingPanel);
        
        setVisible(true);
    }
    
    @Override
    public void update(double deltaTime) {
        if (isPaused) {
            return;
        }
        
        for (Light light : lightRays) {
            light.updateVel(deltaTime);
            light.updatePos(deltaTime);
            System.out.println("Light ray norm: " + light.norm());
        }
    }
    
    @Override
    public void render() {
        drawingPanel.repaint();
    }
    
    @Override
    public void start() {
        animationTimer = new Timer((int)(DELTA_TIME * 1000), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update(DELTA_TIME);
                render();
            }
        });
        animationTimer.start();
    }
    
    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        dispose();
    }
    
    private class DrawingPanel extends JPanel {
        private double scale = 50.0;
        private double panX = 0.0;
        private double panY = 0.0;
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(centerX - 10, centerY - 10, 20, 20);
            
            for (Light light : lightRays) {
                drawLightRay(g2d, light, centerX, centerY);
            }
        }
        
        private void drawLightRay(Graphics2D g2d, Light light, int centerX, int centerY) {
            ArrayList<double[]> trajectory = light.getTrajectory();
            
            if (trajectory.size() >= 2) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(1.5f));
                
                int[] prevScreenX = null;
                int[] prevScreenY = null;
                
                for (int i = 0; i < trajectory.size(); i++) {
                    double[] cartPos = trajectory.get(i);
                    double worldX = cartPos[0];
                    double worldY = cartPos[1];
                    
                    int screenX = (int)(centerX + (worldX + panX) * scale);
                    int screenY = (int)(centerY - (worldY + panY) * scale);
                    
                    if (prevScreenX != null && prevScreenY != null) {
                        g2d.drawLine(prevScreenX[0], prevScreenY[0], screenX, screenY);
                    }
                    
                    prevScreenX = new int[] {screenX};
                    prevScreenY = new int[] {screenY};
                }
            }
            
            double[] currentPos = light.getCartesianPos();
            int currentScreenX = (int)(centerX + (currentPos[1] + panX) * scale);
            int currentScreenY = (int)(centerY - (currentPos[2] + panY) * scale);
            
            g2d.setColor(Color.CYAN);
            g2d.fillOval(currentScreenX - 3, currentScreenY - 3, 6, 6);
        }
    }
}

