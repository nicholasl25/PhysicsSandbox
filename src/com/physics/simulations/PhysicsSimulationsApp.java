package com.physics.simulations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main application window - the homepage/landing page.
 */
public class PhysicsSimulationsApp extends JFrame {
    
    public PhysicsSimulationsApp() {
        initializeUI();
    }
    
    /**
     * Sets up the user interface components
     */
    private void initializeUI() {
        // Basic window settings
        setTitle("PhysicsSandbox");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null); // Center the window on screen
        
        // Create and add the main panel
        JPanel mainPanel = createHomePage();
        add(mainPanel);
    }
    
    /**
     * Creates the homepage panel with title and simulation button
     */
    private JPanel createHomePage() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // Title
        JLabel titleLabel = new JLabel("PhysicsSandbox");
        titleLabel.setFont(new Font("Sans-serif", Font.BOLD, 48));
        titleLabel.setForeground(new Color(40, 40, 40));
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);
        
        // Spacer
        gbc.gridy = 1;
        gbc.insets = new Insets(40, 20, 20, 20);
        panel.add(Box.createVerticalStrut(20), gbc);
        
        // Simulation button
        JButton gravityButton = new JButton("2D Gravity Simulation");
        gravityButton.setFont(new Font("Sans-serif", Font.PLAIN, 20));
        gravityButton.setPreferredSize(new Dimension(350, 70));
        gravityButton.setBackground(new Color(60, 60, 60));
        gravityButton.setForeground(Color.WHITE);
        gravityButton.setOpaque(true);
        gravityButton.setFocusPainted(false);
        gravityButton.setBorderPainted(false);
        gravityButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        gravityButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                gravityButton.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                gravityButton.setBackground(new Color(60, 60, 60));
            }
        });
        
        gravityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    com.physics.simulations.NewtonianGravity.GravitySimulation gravitySim = 
                        new com.physics.simulations.NewtonianGravity.GravitySimulation();
                    gravitySim.start();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        PhysicsSimulationsApp.this,
                        "Error launching simulation:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 20, 20, 20);
        panel.add(gravityButton, gbc);
        
        // Description
        JLabel descriptionLabel = new JLabel(
            "<html><div style='text-align: center; width: 350px;'>" +
            "Simulate gravitational interactions between planets and masses.<br>" +
            "Click to place objects, drag to pan, and watch physics in action." +
            "</div></html>"
        );
        descriptionLabel.setFont(new Font("Sans-serif", Font.PLAIN, 13));
        descriptionLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 20, 20, 20);
        panel.add(descriptionLabel, gbc);
        
        // Schwarzschild simulation button
        JButton schwarzschildButton = new JButton("Schwarzschild Black Hole Simulation");
        schwarzschildButton.setFont(new Font("Sans-serif", Font.PLAIN, 20));
        schwarzschildButton.setPreferredSize(new Dimension(350, 70));
        schwarzschildButton.setBackground(new Color(60, 60, 60));
        schwarzschildButton.setForeground(Color.WHITE);
        schwarzschildButton.setOpaque(true);
        schwarzschildButton.setFocusPainted(false);
        schwarzschildButton.setBorderPainted(false);
        schwarzschildButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        schwarzschildButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                schwarzschildButton.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                schwarzschildButton.setBackground(new Color(60, 60, 60));
            }
        });
        
        schwarzschildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    com.physics.simulations.Schwarzchild.SchwarzchildSimulation schwarzschildSim = 
                        new com.physics.simulations.Schwarzchild.SchwarzchildSimulation();
                    schwarzschildSim.initialize();
                    schwarzschildSim.start();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        PhysicsSimulationsApp.this,
                        "Error launching simulation:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        gbc.gridy = 4;
        gbc.insets = new Insets(30, 20, 20, 20);
        panel.add(schwarzschildButton, gbc);
        
        // Schwarzschild description
        JLabel schwarzschildDescriptionLabel = new JLabel(
            "<html><div style='text-align: center; width: 350px;'>" +
            "Simulate light rays and geodesics around a Schwarzschild black hole.<br>" +
            "Visualize general relativity effects using tensor calculus." +
            "</div></html>"
        );
        schwarzschildDescriptionLabel.setFont(new Font("Sans-serif", Font.PLAIN, 13));
        schwarzschildDescriptionLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 20, 20, 20);
        panel.add(schwarzschildDescriptionLabel, gbc);
        
        return panel;
    }
    
}
