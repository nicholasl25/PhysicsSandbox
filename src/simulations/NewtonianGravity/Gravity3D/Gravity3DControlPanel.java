package simulations.NewtonianGravity.Gravity3D;

import simulations.BaseControlPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Control panel for the 3D gravity simulation.
 * Handles user input for adding planets and adjusting simulation parameters.
 */
public class Gravity3DControlPanel extends BaseControlPanel {
    // Input fields
    private JTextField massField, radiusField, xField, yField, zField, vxField, vyField, vzField, periodField, nameField, temperatureField;
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
     */
    public Gravity3DControlPanel(Runnable onAddPlanet, Runnable onClearSimulation, 
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
        JPanel contentPanel = getContentPanel();
        
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
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
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
        
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
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
        
        // Position X, Y, Z
        JLabel positionLabel = new JLabel("Position:");
        positionLabel.setForeground(Color.WHITE);
        positionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(positionLabel);
        
        JPanel positionPanel = new JPanel();
        positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.X_AXIS));
        positionPanel.setBackground(new Color(50, 50, 50));
        positionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        positionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, positionPanel.getPreferredSize().height));
        
        JLabel xLabel = new JLabel("X:");
        xLabel.setForeground(Color.WHITE);
        positionPanel.add(xLabel);
        positionPanel.add(Box.createHorizontalStrut(5));
        xField = new JTextField("0.0");
        xField.setPreferredSize(new Dimension(70, xField.getPreferredSize().height));
        xField.setMaximumSize(new Dimension(70, xField.getPreferredSize().height));
        positionPanel.add(xField);
        positionPanel.add(Box.createHorizontalStrut(5));
        
        JLabel yLabel = new JLabel("Y:");
        yLabel.setForeground(Color.WHITE);
        positionPanel.add(yLabel);
        positionPanel.add(Box.createHorizontalStrut(5));
        yField = new JTextField("0.0");
        yField.setPreferredSize(new Dimension(70, yField.getPreferredSize().height));
        yField.setMaximumSize(new Dimension(70, yField.getPreferredSize().height));
        positionPanel.add(yField);
        positionPanel.add(Box.createHorizontalStrut(5));
        
        JLabel zLabel = new JLabel("Z:");
        zLabel.setForeground(Color.WHITE);
        positionPanel.add(zLabel);
        positionPanel.add(Box.createHorizontalStrut(5));
        zField = new JTextField("0.0");
        zField.setPreferredSize(new Dimension(70, zField.getPreferredSize().height));
        zField.setMaximumSize(new Dimension(70, zField.getPreferredSize().height));
        positionPanel.add(zField);
        
        panel.add(positionPanel);
        panel.add(Box.createVerticalStrut(5));
        
        // Velocity Vx, Vy, Vz
        JLabel velocityLabel = new JLabel("Velocity:");
        velocityLabel.setForeground(Color.WHITE);
        velocityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(velocityLabel);
        
        JPanel velocityPanel = new JPanel();
        velocityPanel.setLayout(new BoxLayout(velocityPanel, BoxLayout.X_AXIS));
        velocityPanel.setBackground(new Color(50, 50, 50));
        velocityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        velocityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, velocityPanel.getPreferredSize().height));
        
        JLabel vxLabel = new JLabel("Vx:");
        vxLabel.setForeground(Color.WHITE);
        velocityPanel.add(vxLabel);
        velocityPanel.add(Box.createHorizontalStrut(5));
        vxField = new JTextField("0.0");
        vxField.setPreferredSize(new Dimension(70, vxField.getPreferredSize().height));
        vxField.setMaximumSize(new Dimension(70, vxField.getPreferredSize().height));
        velocityPanel.add(vxField);
        velocityPanel.add(Box.createHorizontalStrut(5));
        
        JLabel vyLabel = new JLabel("Vy:");
        vyLabel.setForeground(Color.WHITE);
        velocityPanel.add(vyLabel);
        velocityPanel.add(Box.createHorizontalStrut(5));
        vyField = new JTextField("0.0");
        vyField.setPreferredSize(new Dimension(70, vyField.getPreferredSize().height));
        vyField.setMaximumSize(new Dimension(70, vyField.getPreferredSize().height));
        velocityPanel.add(vyField);
        velocityPanel.add(Box.createHorizontalStrut(5));
        
        JLabel vzLabel = new JLabel("Vz:");
        vzLabel.setForeground(Color.WHITE);
        velocityPanel.add(vzLabel);
        velocityPanel.add(Box.createHorizontalStrut(5));
        vzField = new JTextField("0.0");
        vzField.setPreferredSize(new Dimension(70, vzField.getPreferredSize().height));
        vzField.setMaximumSize(new Dimension(70, vzField.getPreferredSize().height));
        velocityPanel.add(vzField);
        
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
        fixedLocationDesc.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
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
        JLabel gravityLabel = new JLabel("Gravitational Constant:");
        gravityLabel.setForeground(Color.WHITE);
        gravityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(gravityLabel);
        
        gravitySlider = new JSlider(0, 10000, 6000);
        gravitySlider.setMajorTickSpacing(2000);
        gravitySlider.setPaintTicks(true);
        gravitySlider.setPaintLabels(true);
        gravitySlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        gravitySlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, gravitySlider.getPreferredSize().height));
        gravitySlider.addChangeListener(e -> {
            if (onGravityChanged != null) {
                onGravityChanged.accept((double) gravitySlider.getValue());
            }
        });
        panel.add(gravitySlider);
        
        JLabel gravityValueLabel = new JLabel("Value: " + gravitySlider.getValue());
        gravityValueLabel.setForeground(Color.LIGHT_GRAY);
        gravityValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        gravitySlider.addChangeListener(e -> {
            gravityValueLabel.setText("Value: " + gravitySlider.getValue());
        });
        panel.add(gravityValueLabel);
        
        panel.add(Box.createVerticalStrut(15));
        
        // Time Factor slider
        JLabel timeFactorLabel = new JLabel("Time Factor:");
        timeFactorLabel.setForeground(Color.WHITE);
        timeFactorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(timeFactorLabel);
        
        timeFactorSlider = new JSlider(0, 200, 100);
        timeFactorSlider.setMajorTickSpacing(50);
        timeFactorSlider.setPaintTicks(true);
        timeFactorSlider.setPaintLabels(true);
        timeFactorSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeFactorSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, timeFactorSlider.getPreferredSize().height));
        timeFactorSlider.addChangeListener(e -> {
            if (onTimeFactorChanged != null) {
                onTimeFactorChanged.accept(timeFactorSlider.getValue() / 100.0);
            }
        });
        panel.add(timeFactorSlider);
        
        JLabel timeFactorValueLabel = new JLabel("Value: " + String.format("%.2f", timeFactorSlider.getValue() / 100.0));
        timeFactorValueLabel.setForeground(Color.LIGHT_GRAY);
        timeFactorValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeFactorSlider.addChangeListener(e -> {
            timeFactorValueLabel.setText("Value: " + String.format("%.2f", timeFactorSlider.getValue() / 100.0));
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
            double x = Double.parseDouble(xField.getText());
            double y = Double.parseDouble(yField.getText());
            double z = Double.parseDouble(zField.getText());
            double vx = Double.parseDouble(vxField.getText());
            double vy = Double.parseDouble(vyField.getText());
            double vz = Double.parseDouble(vzField.getText());
            double period = Double.parseDouble(periodField.getText());
            double temperature = Double.parseDouble(temperatureField.getText());
            String texturePath = getTexturePath();
            String name = nameField.getText().trim();
            boolean fixedLocation = fixedLocationCheckBox.isSelected();
            
            // Get color based on texture selection
            Color color = getColorFromTexture();
            
            return new PlanetData(mass, radius, x, y, z, vx, vy, vz, period, temperature, color, texturePath, name, fixedLocation);
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
     */
    private Color getColorFromTexture() {
        String selected = (String) textureCombo.getSelectedItem();
        if (selected == null || selected.equals("No Texture")) {
            return Color.BLUE;
        }
        
        switch (selected) {
            case "Earth": return new Color(30, 100, 200);
            case "Mars": return new Color(200, 50, 30);
            case "Jupiter": return new Color(200, 150, 100);
            case "Moon": return new Color(180, 180, 180);
            case "Sun": return Color.YELLOW;
            case "Venus": return new Color(255, 200, 100);
            default: return Color.BLUE;
        }
    }
    
    /**
     * Data class for planet creation.
     */
    public static class PlanetData {
        public final double mass;
        public final double radius;
        public final double x, y, z;
        public final double vx, vy, vz;
        public final double period;
        public final double temperature;
        public final Color color;
        public final String texturePath;
        public final String name;
        public final boolean fixedLocation;
        
        public PlanetData(double mass, double radius, double x, double y, double z, 
                         double vx, double vy, double vz, double period, double temperature, 
                         Color color, String texturePath, String name, boolean fixedLocation) {
            this.mass = mass;
            this.radius = radius;
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.period = period;
            this.temperature = temperature;
            this.color = color;
            this.texturePath = texturePath;
            this.name = name;
            this.fixedLocation = fixedLocation;
        }
        
        /**
         * Calculates angular velocity from the period of rotation.
         */
        public double getAngularVelocity() {
            if (period <= 0) {
                return 0.0;
            }
            return (2.0 * Math.PI) / period;
        }
    }
}
