package com.physics.simulations.gravity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Control panel for the gravity simulation.
 * Handles user input for adding planets and point masses.
 */
public class ControlPanel extends JPanel {
    // Input fields
    private JTextField massField, radiusField, vxField, vyField;
    private JComboBox<String> colorCombo;
    private JSlider gravitySlider;
    
    // Callbacks
    private Runnable onAddPlanet;
    private Runnable onAddStationaryMass;
    private Runnable onClearSimulation;
    private java.util.function.Consumer<Double> onGravityChanged;
    
    /**
     * Creates a new control panel with the specified callbacks.
     * 
     * @param onAddPlanet Called when "Add Planet" button is clicked
     * @param onAddStationaryMass Called when "Add Stationary Mass" button is clicked
     * @param onClearSimulation Called when "Clear Simulation" button is clicked
     * @param onGravityChanged Called when gravity slider changes
     */
    public ControlPanel(Runnable onAddPlanet, Runnable onAddStationaryMass, Runnable onClearSimulation, 
                       java.util.function.Consumer<Double> onGravityChanged) {
        this.onAddPlanet = onAddPlanet;
        this.onAddStationaryMass = onAddStationaryMass;
        this.onClearSimulation = onClearSimulation;
        this.onGravityChanged = onGravityChanged;
        
        setupPanel();
    }
    
    /**
     * Sets up the control panel UI.
     */
    private void setupPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50));
        
        // Set preferred width
        setPreferredSize(new Dimension(280, 0));
        
        // Create tabbed pane for Add and Settings
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(50, 50, 50));
        tabbedPane.setForeground(Color.WHITE);
        
        // Add tab
        JPanel addPanel = createAddPanel();
        tabbedPane.addTab("Add", addPanel);
        
        // Settings tab
        JPanel settingsPanel = createSettingsPanel();
        tabbedPane.addTab("Settings", settingsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Clear button at bottom
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(new Color(50, 50, 50));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton clearSimulationButton = new JButton("Clear Simulation");
        clearSimulationButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, clearSimulationButton.getPreferredSize().height));
        clearSimulationButton.setForeground(Color.RED);
        clearSimulationButton.addActionListener(e -> {
            if (onClearSimulation != null) {
                onClearSimulation.run();
            }
        });
        removeSpacebarActivation(clearSimulationButton);
        bottomPanel.add(clearSimulationButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the Add panel.
     */
    private JPanel createAddPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(50, 50, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Mass
        JLabel massLabel = new JLabel("Mass:");
        massLabel.setForeground(Color.WHITE);
        panel.add(massLabel);
        massField = new JTextField("50.0");
        massField.setMaximumSize(new Dimension(Integer.MAX_VALUE, massField.getPreferredSize().height));
        panel.add(massField);
        panel.add(Box.createVerticalStrut(5));
        
        // Radius
        JLabel radiusLabel = new JLabel("Radius:");
        radiusLabel.setForeground(Color.WHITE);
        panel.add(radiusLabel);
        radiusField = new JTextField("10.0");
        radiusField.setMaximumSize(new Dimension(Integer.MAX_VALUE, radiusField.getPreferredSize().height));
        panel.add(radiusField);
        panel.add(Box.createVerticalStrut(5));
        
        // VX
        JLabel vxLabel = new JLabel("VX:");
        vxLabel.setForeground(Color.WHITE);
        panel.add(vxLabel);
        vxField = new JTextField("0.0");
        vxField.setMaximumSize(new Dimension(Integer.MAX_VALUE, vxField.getPreferredSize().height));
        panel.add(vxField);
        panel.add(Box.createVerticalStrut(5));
        
        // VY
        JLabel vyLabel = new JLabel("VY:");
        vyLabel.setForeground(Color.WHITE);
        panel.add(vyLabel);
        vyField = new JTextField("0.0");
        vyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, vyField.getPreferredSize().height));
        panel.add(vyField);
        panel.add(Box.createVerticalStrut(5));
        
        // Color
        JLabel colorLabel = new JLabel("Color:");
        colorLabel.setForeground(Color.WHITE);
        panel.add(colorLabel);
        colorCombo = new JComboBox<>(new String[]{
            "Blue", "Red", "Green", "Yellow", "Orange", "Purple", "Cyan", "White"
        });
        colorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, colorCombo.getPreferredSize().height));
        panel.add(colorCombo);
        panel.add(Box.createVerticalStrut(10));
        
        // Add Planet button
        JButton addPlanetButton = new JButton("Add Planet");
        addPlanetButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addPlanetButton.getPreferredSize().height));
        addPlanetButton.addActionListener(e -> {
            if (onAddPlanet != null) {
                onAddPlanet.run();
            }
        });
        removeSpacebarActivation(addPlanetButton);
        panel.add(addPlanetButton);
        panel.add(Box.createVerticalStrut(5));
        
        // Add Stationary Mass button
        JButton addStationaryMassButton = new JButton("Add Stationary Mass");
        addStationaryMassButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addStationaryMassButton.getPreferredSize().height));
        addStationaryMassButton.addActionListener(e -> {
            if (onAddStationaryMass != null) {
                onAddStationaryMass.run();
            }
        });
        removeSpacebarActivation(addStationaryMassButton);
        panel.add(addStationaryMassButton);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Creates the Settings panel.
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(50, 50, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Gravity slider
        JLabel gLabel = new JLabel("Gravitational Constant (G):");
        gLabel.setForeground(Color.WHITE);
        panel.add(gLabel);
        
        gravitySlider = new JSlider(0, 10000, 6000);
        gravitySlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, gravitySlider.getPreferredSize().height));
        gravitySlider.setBackground(new Color(50, 50, 50));
        gravitySlider.setForeground(Color.WHITE);
        gravitySlider.addChangeListener(e -> {
            if (onGravityChanged != null) {
                onGravityChanged.accept((double) gravitySlider.getValue());
            }
        });
        panel.add(gravitySlider);
        
        JLabel gValueLabel = new JLabel("G = 6000");
        gValueLabel.setForeground(Color.LIGHT_GRAY);
        gValueLabel.setFont(new Font("Sans-serif", Font.PLAIN, 11));
        gravitySlider.addChangeListener(e -> {
            gValueLabel.setText("G = " + gravitySlider.getValue());
        });
        panel.add(gValueLabel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    
    /**
     * Gets planet data from the control fields.
     * 
     * @return PlanetData object containing the values, or null if invalid
     */
    public PlanetData getPlanetData() {
        try {
            double mass = Double.parseDouble(massField.getText());
            double radius = Double.parseDouble(radiusField.getText());
            double vx = Double.parseDouble(vxField.getText());
            double vy = Double.parseDouble(vyField.getText());
            Color color = getColorFromCombo(colorCombo);
            
            return new PlanetData(mass, radius, vx, vy, color);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Gets stationary mass data from the control fields.
     * 
     * @return StationaryMassData object containing the values, or null if invalid
     */
    public StationaryMassData getStationaryMassData() {
        try {
            double mass = Double.parseDouble(massField.getText());
            double radius = Double.parseDouble(radiusField.getText());
            Color color = getColorFromCombo(colorCombo);
            
            return new StationaryMassData(mass, radius, color);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Gets the selected color from the combo box.
     */
    private Color getColorFromCombo(JComboBox<String> combo) {
        if (combo == null || combo.getSelectedItem() == null) return Color.BLUE;
        String colorName = (String) combo.getSelectedItem();
        switch (colorName) {
            case "Blue": return Color.BLUE;
            case "Red": return Color.RED;
            case "Green": return Color.GREEN;
            case "Yellow": return Color.YELLOW;
            case "Orange": return Color.ORANGE;
            case "Purple": return Color.MAGENTA;
            case "Cyan": return Color.CYAN;
            case "White": return Color.WHITE;
            default: return Color.BLUE;
        }
    }
    
    /**
     * Removes spacebar activation from a button.
     * This prevents the button from being activated when spacebar is pressed,
     * ensuring spacebar is reserved for pause/resume in the simulation.
     */
    private void removeSpacebarActivation(JButton button) {
        // Remove spacebar from button's input map
        InputMap inputMap = button.getInputMap(JComponent.WHEN_FOCUSED);
        KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        inputMap.put(spaceKey, "none");
        
        // Also remove from ancestor input map
        InputMap ancestorMap = button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (ancestorMap != null) {
            ancestorMap.put(spaceKey, "none");
        }
    }
    
    /**
     * Data class for planet creation.
     */
    public static class PlanetData {
        public final double mass;
        public final double radius;
        public final double vx;
        public final double vy;
        public final Color color;
        
        public PlanetData(double mass, double radius, double vx, double vy, Color color) {
            this.mass = mass;
            this.radius = radius;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
        }
    }
    
    /**
     * Data class for stationary mass creation.
     */
    public static class StationaryMassData {
        public final double mass;
        public final double radius;
        public final Color color;
        
        public StationaryMassData(double mass, double radius, Color color) {
            this.mass = mass;
            this.radius = radius;
            this.color = color;
        }
    }
}

