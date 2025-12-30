package simulations.NewtonianGravity.Gravity2D;

import simulations.BaseControlPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Control panel for the gravity simulation.
 * Handles user input for adding planets and point masses.
 */
public class Gravity2DControlPanel extends BaseControlPanel {
    // Input fields
    private JTextField massField, radiusField, vxField, vyField, periodField, nameField, temperatureField;
    private JComboBox<String> textureCombo;
    private JSlider gravitySlider, timeFactorSlider;
    private JPanel advancedPanel;
    private boolean advancedExpanded = false;
    private JCheckBox fixedLocationCheckBox;
    private JCheckBox bounceCheckBox;
    private JCheckBox rk4CheckBox;
    
    // Callbacks
    private Runnable onAddPlanet;
    private Runnable onClearSimulation;
    private java.util.function.Consumer<Double> onGravityChanged;
    private java.util.function.Consumer<Double> onTimeFactorChanged;
    private java.util.function.Consumer<Boolean> onBounceChanged;
    private java.util.function.Consumer<Boolean> onRK4Changed;
    
    /**
     * Creates a new control panel with the specified callbacks.
     * 
     * @param onAddPlanet Called when "Add Planet" button is clicked
     * @param onClearSimulation Called when "Clear Simulation" button is clicked
     * @param onGravityChanged Called when gravity slider changes
     * @param onTimeFactorChanged Called when time factor slider changes
     * @param onBounceChanged Called when bounce checkbox changes
     * @param onRK4Changed Called when RK4 checkbox changes
     */
    public Gravity2DControlPanel(Runnable onAddPlanet, Runnable onClearSimulation, 
                       java.util.function.Consumer<Double> onGravityChanged,
                       java.util.function.Consumer<Double> onTimeFactorChanged,
                       java.util.function.Consumer<Boolean> onBounceChanged,
                       java.util.function.Consumer<Boolean> onRK4Changed) {
        this.onAddPlanet = onAddPlanet;
        this.onClearSimulation = onClearSimulation;
        this.onGravityChanged = onGravityChanged;
        this.onTimeFactorChanged = onTimeFactorChanged;
        this.onBounceChanged = onBounceChanged;
        this.onRK4Changed = onRK4Changed;
    }
    
    /**
     * Sets up the control panel UI.
     */
    @Override
    protected void setupContent() {
        
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
        
        // Name (optional)
        JLabel nameLabel = new JLabel("Name (optional):");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(nameLabel);
        nameField = new JTextField("");
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setToolTipText("Leave empty for automatic naming (Planet #N)");
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(5));
        
        // Mass
        JLabel massLabel = new JLabel("Mass:");
        massLabel.setForeground(Color.WHITE);
        massLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(massLabel);
        massField = new JTextField("50.0");
        massField.setMaximumSize(new Dimension(Integer.MAX_VALUE, massField.getPreferredSize().height));
        massField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(massField);
        panel.add(Box.createVerticalStrut(5));
        
        // Radius
        JLabel radiusLabel = new JLabel("Radius:");
        radiusLabel.setForeground(Color.WHITE);
        radiusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(radiusLabel);
        radiusField = new JTextField("10.0");
        radiusField.setMaximumSize(new Dimension(Integer.MAX_VALUE, radiusField.getPreferredSize().height));
        radiusField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(radiusField);
        panel.add(Box.createVerticalStrut(5));
        
        // Velocity X and Y on the same line
        JPanel velocityPanel = new JPanel();
        velocityPanel.setLayout(new BoxLayout(velocityPanel, BoxLayout.X_AXIS));
        velocityPanel.setBackground(new Color(50, 50, 50));
        velocityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        velocityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, velocityPanel.getPreferredSize().height));
        
        // Velocity X
        JLabel vxLabel = new JLabel("Vx:");
        vxLabel.setForeground(Color.WHITE);
        velocityPanel.add(vxLabel);
        velocityPanel.add(Box.createHorizontalStrut(5));
        vxField = new JTextField("0.0");
        vxField.setPreferredSize(new Dimension(80, vxField.getPreferredSize().height));
        vxField.setMaximumSize(new Dimension(80, vxField.getPreferredSize().height));
        velocityPanel.add(vxField);
        velocityPanel.add(Box.createHorizontalStrut(10));
        
        // Velocity Y
        JLabel vyLabel = new JLabel("Vy:");
        vyLabel.setForeground(Color.WHITE);
        velocityPanel.add(vyLabel);
        velocityPanel.add(Box.createHorizontalStrut(5));
        vyField = new JTextField("0.0");
        vyField.setPreferredSize(new Dimension(80, vyField.getPreferredSize().height));
        vyField.setMaximumSize(new Dimension(80, vyField.getPreferredSize().height));
        velocityPanel.add(vyField);
        
        panel.add(velocityPanel);
        panel.add(Box.createVerticalStrut(5));
        
        // Texture
        JLabel textureLabel = new JLabel("Texture:");
        textureLabel.setForeground(Color.WHITE);
        textureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(textureLabel);
        textureCombo = new JComboBox<>(new String[]{
            "No Texture", "Earth", "Mars", "Jupiter", "Moon", "Sun", "Venus"
        });
        textureCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, textureCombo.getPreferredSize().height));
        textureCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(textureCombo);
        panel.add(Box.createVerticalStrut(10));
        
        // Advanced Settings collapsible section
        JPanel advancedHeader = new JPanel();
        advancedHeader.setLayout(new BorderLayout());
        advancedHeader.setBackground(new Color(60, 60, 60));
        advancedHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        advancedHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        advancedHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel advancedLabel = new JLabel("▶ Advanced Settings");
        advancedLabel.setForeground(Color.LIGHT_GRAY);
        advancedLabel.setFont(new Font("Sans-serif", Font.BOLD, 11));
        advancedHeader.add(advancedLabel, BorderLayout.WEST);
        
        // Make the header clickable
        advancedHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        advancedHeader.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                advancedExpanded = !advancedExpanded;
                advancedLabel.setText(advancedExpanded ? "▼ Advanced Settings" : "▶ Advanced Settings");
                advancedPanel.setVisible(advancedExpanded);
                panel.revalidate();
                panel.repaint();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                advancedHeader.setBackground(new Color(70, 70, 70));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                advancedHeader.setBackground(new Color(60, 60, 60));
            }
        });
        
        panel.add(advancedHeader);
        
        // Advanced settings content panel
        advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        advancedPanel.setBackground(new Color(50, 50, 50));
        advancedPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        advancedPanel.setVisible(false);
        
        // Period of Rotation field
        JLabel periodLabel = new JLabel("Period of Rotation (T):");
        periodLabel.setForeground(Color.WHITE);
        periodLabel.setFont(new Font("Sans-serif", Font.PLAIN, 11));
        periodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        advancedPanel.add(periodLabel);
        
        periodField = new JTextField("100.0");
        periodField.setMaximumSize(new Dimension(Integer.MAX_VALUE, periodField.getPreferredSize().height));
        periodField.setAlignmentX(Component.LEFT_ALIGNMENT);
        periodField.setToolTipText("Time for one complete rotation (seconds)");
        advancedPanel.add(periodField);
        advancedPanel.add(Box.createVerticalStrut(10));
        
        // Temperature field
        JLabel temperatureLabel = new JLabel("Temperature (K):");
        temperatureLabel.setForeground(Color.WHITE);
        temperatureLabel.setFont(new Font("Sans-serif", Font.PLAIN, 11));
        temperatureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        advancedPanel.add(temperatureLabel);
        
        temperatureField = new JTextField("300.0");
        temperatureField.setMaximumSize(new Dimension(Integer.MAX_VALUE, temperatureField.getPreferredSize().height));
        temperatureField.setAlignmentX(Component.LEFT_ALIGNMENT);
        temperatureField.setToolTipText("Temperature in Kelvin");
        advancedPanel.add(temperatureField);
        advancedPanel.add(Box.createVerticalStrut(10));
        
        // Fixed Location checkbox
        fixedLocationCheckBox = new JCheckBox("Fixed Location");
        fixedLocationCheckBox.setForeground(Color.WHITE);
        fixedLocationCheckBox.setBackground(new Color(50, 50, 50));
        fixedLocationCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedLocationCheckBox.setToolTipText("Creates a stationary PointMass instead of a moving Planet");
        advancedPanel.add(fixedLocationCheckBox);
        
        // Description for Fixed Location
        JLabel fixedLocationDesc = new JLabel("<html><div style='width:200px'>Creates a stationary mass that<br>exerts gravity but doesn't move</div></html>");
        fixedLocationDesc.setForeground(Color.LIGHT_GRAY);
        fixedLocationDesc.setFont(new Font("Sans-serif", Font.PLAIN, 10));
        fixedLocationDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixedLocationDesc.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0)); // Indent to align with checkbox
        advancedPanel.add(fixedLocationDesc);
        
        panel.add(advancedPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Add Planet button
        JButton addPlanetButton = new JButton("Add Planet");
        addPlanetButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addPlanetButton.getPreferredSize().height));
        addPlanetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPlanetButton.addActionListener(e -> {
            if (onAddPlanet != null) {
                onAddPlanet.run();
            }
        });
        removeSpacebarActivation(addPlanetButton);
        panel.add(addPlanetButton);
        
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
        
        gravitySlider = new JSlider(0, 20000, 6000);
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
        
        panel.add(Box.createVerticalStrut(15));
        
        // Time Factor slider
        JLabel timeFactorLabel = new JLabel("Time Factor:");
        timeFactorLabel.setForeground(Color.WHITE);
        panel.add(timeFactorLabel);
        
        timeFactorSlider = new JSlider(1, 100, 10); // 0.1 to 10.0 (scaled by 10)
        timeFactorSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, timeFactorSlider.getPreferredSize().height));
        timeFactorSlider.setBackground(new Color(50, 50, 50));
        timeFactorSlider.setForeground(Color.WHITE);
        timeFactorSlider.addChangeListener(e -> {
            if (onTimeFactorChanged != null) {
                double timeFactor = timeFactorSlider.getValue() / 10.0;
                onTimeFactorChanged.accept(timeFactor);
            }
        });
        panel.add(timeFactorSlider);
        
        JLabel timeFactorValueLabel = new JLabel("1.0 s/s");
        timeFactorValueLabel.setForeground(Color.LIGHT_GRAY);
        timeFactorValueLabel.setFont(new Font("Sans-serif", Font.PLAIN, 11));
        timeFactorSlider.addChangeListener(e -> {
            double timeFactor = timeFactorSlider.getValue() / 10.0;
            timeFactorValueLabel.setText(String.format("%.1f s/s", timeFactor));
        });
        panel.add(timeFactorValueLabel);
        
        panel.add(Box.createVerticalStrut(15));
        
        // Bounce checkbox
        bounceCheckBox = new JCheckBox("Bounce");
        bounceCheckBox.setForeground(Color.WHITE);
        bounceCheckBox.setBackground(new Color(50, 50, 50));
        bounceCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        bounceCheckBox.setToolTipText("When enabled, planets bounce on collision instead of merging");
        bounceCheckBox.addActionListener(e -> {
            if (onBounceChanged != null) {
                onBounceChanged.accept(bounceCheckBox.isSelected());
            }
        });
        panel.add(bounceCheckBox);
        
        panel.add(Box.createVerticalStrut(10));
        
        // RK4 checkbox
        rk4CheckBox = new JCheckBox("RK4 Integration");
        rk4CheckBox.setForeground(Color.WHITE);
        rk4CheckBox.setBackground(new Color(50, 50, 50));
        rk4CheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        rk4CheckBox.setToolTipText("When enabled, uses Runge-Kutta 4th order integration for more accurate physics");
        rk4CheckBox.addActionListener(e -> {
            if (onRK4Changed != null) {
                onRK4Changed.accept(rk4CheckBox.isSelected());
            }
        });
        panel.add(rk4CheckBox);
        
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
            double period = Double.parseDouble(periodField.getText());
            double temperature = Double.parseDouble(temperatureField.getText());
            String texturePath = getTexturePath();
            String name = nameField.getText().trim(); // Get name, empty string if not provided
            boolean fixedLocation = fixedLocationCheckBox.isSelected();
            
            // Get color based on texture selection (default Blue if no texture)
            Color color = getColorFromTexture();
            
            return new PlanetData(mass, radius, vx, vy, period, temperature, color, texturePath, name, fixedLocation);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Gets the texture path based on selection
     */
    private String getTexturePath() {
        String selected = (String) textureCombo.getSelectedItem();
        if (selected == null || selected.equals("No Texture")) {
            return null;
        }
        return "resources/textures/" + selected + ".jpg";
    }
    
    /**
     * Gets the color based on texture selection.
     * When "No Texture" is selected, returns a default color (Blue).
     * Otherwise, returns a color that matches the texture theme.
     */
    private Color getColorFromTexture() {
        String selected = (String) textureCombo.getSelectedItem();
        if (selected == null || selected.equals("No Texture")) {
            return Color.BLUE; // Default color when no texture
        }
        
        // Map texture names to appropriate colors
        switch (selected) {
            case "Earth": return new Color(30, 100, 200); // Blue-green
            case "Mars": return new Color(200, 50, 30); // Red-orange
            case "Jupiter": return new Color(200, 150, 100); // Brown-orange
            case "Moon": return new Color(180, 180, 180); // Gray
            case "Sun": return Color.YELLOW;
            case "Venus": return new Color(255, 200, 100); // Yellow-orange
            default: return Color.BLUE;
        }
    }
    
    /**
     * Removes spacebar activation from a button.
     * This prevents the button from being activated when spacebar is pressed,
     * ensuring spacebar is reserved for pause/resume in the simulation.
     */
    @Override
    protected void removeSpacebarActivation(JButton button) {
        super.removeSpacebarActivation(button);
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
        public final double period;  // Period of rotation in seconds
        public final double temperature;  // Temperature in Kelvin
        public final Color color;
        public final String texturePath;
        public final String name;  // Planet name (empty string if not provided)
        public final boolean fixedLocation;  // If true, creates a PointMass instead of Planet
        
        public PlanetData(double mass, double radius, double vx, double vy, double period, double temperature, Color color, String texturePath, String name, boolean fixedLocation) {
            this.mass = mass;
            this.radius = radius;
            this.vx = vx;
            this.vy = vy;
            this.period = period;
            this.temperature = temperature;
            this.color = color;
            this.texturePath = texturePath;
            this.name = name;
            this.fixedLocation = fixedLocation;
        }
        
        /**
         * Calculates angular velocity from the period of rotation.
         * Formula: ω = 2π / T
         * 
         * @return Angular velocity in radians per second
         */
        public double getAngularVelocity() {
            if (period <= 0) {
                return 0.0;
            }
            return (2.0 * Math.PI) / period;
        }
    }
    
}

