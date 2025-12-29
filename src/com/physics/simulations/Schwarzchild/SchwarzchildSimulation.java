package com.physics.simulations.Schwarzchild;

import com.physics.simulations.BaseSimulation;

/**
 * Simulation of light rays (null geodesics) in Schwarzschild spacetime.
 * Visualizes gravitational lensing and light trajectories around a black hole.
 */
import javax.swing.*;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class SchwarzchildSimulation extends BaseSimulation {
    
    private Timer animationTimer;
    private static final double DELTA_TIME = 1.0 / 60.0;
    
    private DrawingPanel drawingPanel;
    
    private ArrayList<Light> lightRays;
    
    private boolean isPaused = false;
    private Light selectedLight = null;
    
    public double G = 1.0;
    public double M = 1.0;
    
    // ============================================================================
    // SCHWARZSCHILD-SPECIFIC METHODS
    // These methods are specific to the Schwarzschild metric implementation.
    // For a generic GR simulation, these would be abstract or implemented differently.
    // ============================================================================
    
    /**
     * Returns the Schwarzschild metric tensor at radius r.
     */
    public double[][] getMetric(double r) {
        double[][] metric = new double[3][3];
        double f = 1 - 2 * G * M / r;
        
        metric[0][0] = -f;
        metric[1][1] = 1 / f;
        metric[2][2] = r * r;
        
        return metric;
    }
    
    /**
     * Returns the Christoffel connection coefficients for the Schwarzschild metric at radius r.
     */
    public double[][][] getChristoffel(double r) {
        double[][][] chris = new double[3][3][3];
        double f = 1 - 2 * G * M / r;
        
        chris[0][0][1] = M / (f * r * r);
        chris[0][1][0] = M / (f * r * r);
        chris[1][0][0] = (M * f) / (r * r);
        chris[1][1][1] = -M / (f * r * r);
        chris[1][2][2] = -f * r;
        chris[2][2][1] = 1/r;
        chris[2][1][2] = 1/r;
        
        return chris;
    }
    
    /**
     * Returns the Schwarzschild radius (event horizon): 2GM.
     */
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
        double[] angles = {-0.2, -0.1, 0.0, 0.1, -0.5};
        
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
        
        setupMouseListeners();
        setupKeyboardListeners();
        
        setVisible(true);
    }
    
    private void setupMouseListeners() {
        drawingPanel.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    double[] worldCoords = screenToWorld(e.getX(), e.getY());
                    double worldX = worldCoords[0];
                    double worldY = worldCoords[1];
                    
                    selectedLight = null;
                    double minDistance = Double.MAX_VALUE;
                    
                    for (Light light : lightRays) {
                        double[] cartPos = light.getCartesianPos();
                        double lightX = cartPos[1];
                        double lightY = cartPos[2];
                        
                        double distance = Math.sqrt((worldX - lightX) * (worldX - lightX) + 
                                                   (worldY - lightY) * (worldY - lightY));
                        
                        if (distance < 0.3 && distance < minDistance) {
                            minDistance = distance;
                            selectedLight = light;
                        }
                    }
                    
                    drawingPanel.repaint();
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {}
            
            @Override
            public void mouseReleased(MouseEvent e) {}
            
            @Override
            public void mouseEntered(MouseEvent e) {}
            
            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }
    
    private void setupKeyboardListeners() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        inputMap.put(spaceKey, "pauseResume");
        actionMap.put("pauseResume", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPaused = !isPaused;
                drawingPanel.repaint();
            }
        });
    }
    
    private double[] screenToWorld(int screenX, int screenY) {
        int centerX = drawingPanel.getWidth() / 2;
        int centerY = drawingPanel.getHeight() / 2;
        double scale = drawingPanel.getScale();
        double panX = drawingPanel.getPanX();
        double panY = drawingPanel.getPanY();
        double worldX = (screenX - centerX) / scale - panX;
        double worldY = -(screenY - centerY) / scale - panY;
        return new double[] {worldX, worldY};
    }
    
    @Override
    public void update(double deltaTime) {
        if (isPaused) {
            return;
        }
        
        ArrayList<Light> toRemove = new ArrayList<>();
        
        for (Light light : lightRays) {
            light.updateVel(deltaTime);
            light.updatePos(deltaTime);
            
            if (!light.isOutsideEventHorizon()) {
                toRemove.add(light);
            }
        }
        
        lightRays.removeAll(toRemove);
        if (toRemove.contains(selectedLight)) {
            selectedLight = null;
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
        
        public double getScale() { return scale; }
        public double getPanX() { return panX; }
        public double getPanY() { return panY; }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            double schwarzchildRadius = getSchwarzchildRadius();
            int blackHoleRadius = (int)(schwarzchildRadius * scale);
            
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(centerX - blackHoleRadius, centerY - blackHoleRadius, 
                        blackHoleRadius * 2, blackHoleRadius * 2);
            
            for (Light light : lightRays) {
                drawLightRay(g2d, light, centerX, centerY);
            }
            
            if (selectedLight != null) {
                drawLightInfo(g2d, selectedLight);
            }
            
            g2d.setColor(Color.WHITE);
            g2d.drawString("Light Rays: " + lightRays.size(), 10, 20);
            if (isPaused) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("PAUSED - Press SPACE to resume", 10, 35);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString("Press SPACE to pause", 10, 35);
            }
        }
        
        private void drawLightInfo(Graphics2D g2d, Light light) {
            int infoX = getWidth() - 300;
            int infoY = 20;
            int boxWidth = 280;
            int boxHeight = 140;
            
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(infoX, infoY, boxWidth, boxHeight);
            
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawRect(infoX, infoY, boxWidth, boxHeight);
            
            g2d.setColor(Color.WHITE);
            int textY = infoY + 20;
            g2d.drawString("=== SELECTED LIGHT RAY ===", infoX + 10, textY);
            textY += 20;
            
            double[] threePos = light.getThreePos();
            double r = threePos[1];
            double theta = threePos[2];
            g2d.drawString(String.format("Position (r, θ): (%.3f, %.3f)", r, theta), infoX + 10, textY);
            textY += 20;
            
            double[] threeVel = light.getThreeVel();
            g2d.drawString("Velocity (vt, vr, vθ):", infoX + 10, textY);
            textY += 15;
            g2d.drawString(String.format("  vt = %.6f", threeVel[0]), infoX + 10, textY);
            textY += 15;
            g2d.drawString(String.format("  vr = %.6f", threeVel[1]), infoX + 10, textY);
            textY += 15;
            g2d.drawString(String.format("  vθ = %.6f", threeVel[2]), infoX + 10, textY);
            textY += 15;
            
            double norm = light.norm();
            g2d.drawString(String.format("Norm: %.6f", norm), infoX + 10, textY);
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
            
            if (light == selectedLight) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.drawOval(currentScreenX - 8, currentScreenY - 8, 16, 16);
            }
            
            g2d.setColor(Color.CYAN);
            g2d.fillOval(currentScreenX - 3, currentScreenY - 3, 6, 6);
        }
    }
}

