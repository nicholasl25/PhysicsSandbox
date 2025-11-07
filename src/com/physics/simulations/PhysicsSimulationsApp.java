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
        setTitle("Physics Simulations");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
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
        panel.setBackground(new Color(240, 240, 240));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title label
        JLabel titleLabel = new JLabel("Physics Simulations");
        titleLabel.setFont(new Font("Sans-serif", Font.BOLD, 36));
        titleLabel.setForeground(new Color(60, 60, 60)); // Dark gray for good contrast
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Select a simulation to run:");
        subtitleLabel.setFont(new Font("Sans-serif", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(50, 50, 50)); // Dark gray for good contrast
        gbc.gridy = 1;
        gbc.insets = new Insets(30, 10, 50, 10);
        panel.add(subtitleLabel, gbc);
        
        // GRAVITY Simulation Button
        JButton gravityButton = createSimulationButton("Gravity Simulation", 
            "Watch planets interact through gravitational forces");
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(gravityButton, gbc);
        
        gravityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Launch the gravity simulation
                try {
                    com.physics.simulations.gravity.GravitySimulation gravitySim = 
                        new com.physics.simulations.gravity.GravitySimulation();
                    gravitySim.start();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        PhysicsSimulationsApp.this,
                        "Error launching Gravity Simulation:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        
        // Description text
        JLabel descriptionLabel = new JLabel(
            "<html><div style='text-align: center; width: 400px;'>" +
            "Experience gravitational interactions between multiple planets.<br>" +
            "Watch as they orbit and influence each other's motion.</div></html>"
        );
        descriptionLabel.setFont(new Font("Sans-serif", Font.PLAIN, 14));
        descriptionLabel.setForeground(new Color(0, 0, 0)); // Darker gray for better readability
        gbc.gridy = 3;
        gbc.insets = new Insets(30, 10, 10, 10);
        panel.add(descriptionLabel, gbc);
        
        return panel;
    }
    
    /**
     * Helper method to create styled simulation buttons
     */
    private JButton createSimulationButton(String title, String tooltip) {
        JButton button = new JButton(title);
        button.setFont(new Font("Sans-serif", Font.BOLD, 18));
        button.setPreferredSize(new Dimension(400, 100));
        button.setToolTipText(tooltip);
        button.setBackground(new Color(70, 130, 180)); // Steel blue
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 150, 200));
                button.setBorder(BorderFactory.createLoweredBevelBorder());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
                button.setBorder(BorderFactory.createRaisedBevelBorder());
            }
        });
        
        return button;
    }
}
