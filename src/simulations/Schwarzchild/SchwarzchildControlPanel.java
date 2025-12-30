package simulations.Schwarzchild;

import simulations.BaseControlPanel;
import javax.swing.*;
import java.awt.*;

/**
 * Control panel for the Schwarzschild simulation.
 * Handles user input for adjusting physical constants (c, G, M).
 */
public class SchwarzchildControlPanel extends BaseControlPanel {
    private JSlider cSlider, GSlider, MSlider;
    
    // Callbacks
    private java.util.function.Consumer<Double> onCChanged;
    private java.util.function.Consumer<Double> onGChanged;
    private java.util.function.Consumer<Double> onMChanged;
    
    /**
     * Creates a new control panel with the specified callbacks.
     * 
     * @param onCChanged Called when c slider changes
     * @param onGChanged Called when G slider changes
     * @param onMChanged Called when M slider changes
     */
    public SchwarzchildControlPanel(java.util.function.Consumer<Double> onCChanged,
                                   java.util.function.Consumer<Double> onGChanged,
                                   java.util.function.Consumer<Double> onMChanged) {
        this.onCChanged = onCChanged;
        this.onGChanged = onGChanged;
        this.onMChanged = onMChanged;
    }
    
    /**
     * Sets up the control panel UI.
     */
    @Override
    protected void setupContent() {
        // Main panel with sliders
        JPanel mainPanel = createMainPanel();
        
        // Title
        mainPanel.add(createTitleLabel("Physical Constants"));
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Speed of light (c) slider
        mainPanel.add(createSliderPanel("Speed of Light (c)", 0.5, 2.0, 1.0, cSlider -> {
            this.cSlider = cSlider;
            if (onCChanged != null) {
                onCChanged.accept(getSliderValue(cSlider));
            }
        }));
        
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Gravitational constant (G) slider
        mainPanel.add(createSliderPanel("Gravitational Constant (G)", 0.5, 2.0, 0.5, gSlider -> {
            this.GSlider = gSlider;
            if (onGChanged != null) {
                onGChanged.accept(getSliderValue(gSlider));
            }
        }));
        
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Mass (M) slider
        mainPanel.add(createSliderPanel("Black Hole Mass (M)", 0.5, 2.0, 1.0, mSlider -> {
            this.MSlider = mSlider;
            if (onMChanged != null) {
                onMChanged.accept(getSliderValue(mSlider));
            }
        }));
        
        // Add vertical glue to push content to top
        mainPanel.add(Box.createVerticalGlue());
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a slider panel with label and value display.
     */
    private JPanel createSliderPanel(String labelText, double min, double max, double initialValue,
                                     java.util.function.Consumer<JSlider> onSliderCreated) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(50, 50, 50));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Label
        panel.add(createLabel(labelText, 12));
        
        // Value label
        JLabel valueLabel = createLabel(String.format("%.2f", initialValue), 11);
        valueLabel.setForeground(new Color(200, 200, 200));
        panel.add(valueLabel);
        
        // Slider
        JSlider slider = new JSlider();
        slider.setMinimum((int)(min * 100));
        slider.setMaximum((int)(max * 100));
        slider.setValue((int)(initialValue * 100));
        slider.setBackground(new Color(50, 50, 50));
        slider.setForeground(Color.WHITE);
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        slider.addChangeListener(e -> {
            double value = getSliderValue(slider);
            valueLabel.setText(String.format("%.2f", value));
        });
        
        panel.add(Box.createVerticalStrut(5));
        panel.add(slider);
        
        if (onSliderCreated != null) {
            onSliderCreated.accept(slider);
        }
        
        return panel;
    }
    
    /**
     * Gets the double value from a slider (converts from int to double).
     */
    private double getSliderValue(JSlider slider) {
        return slider.getValue() / 100.0;
    }
    
    /**
     * Sets the value of the c slider.
     */
    public void setCValue(double value) {
        if (cSlider != null) {
            cSlider.setValue((int)(value * 100));
        }
    }
    
    /**
     * Sets the value of the G slider.
     */
    public void setGValue(double value) {
        if (GSlider != null) {
            GSlider.setValue((int)(value * 100));
        }
    }
    
    /**
     * Sets the value of the M slider.
     */
    public void setMValue(double value) {
        if (MSlider != null) {
            MSlider.setValue((int)(value * 100));
        }
    }
}

