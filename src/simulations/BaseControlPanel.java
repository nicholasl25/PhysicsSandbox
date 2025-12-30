package simulations;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for all simulation control panels.
 * Provides common styling and layout functionality.
 */
public abstract class BaseControlPanel extends JPanel {
    
    /**
     * Creates a new base control panel.
     */
    public BaseControlPanel() {
        setupBasePanel();
        setupContent();
    }
    
    /**
     * Sets up the base panel styling and layout.
     */
    private void setupBasePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50));
        setPreferredSize(new Dimension(280, 0));
    }
    
    /**
     * Sets up the panel content. Subclasses should override this to add their specific content.
     */
    protected abstract void setupContent();
    
    /**
     * Creates a styled label with the specified text and font size.
     */
    protected JLabel createLabel(String text, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Sans-serif", Font.PLAIN, fontSize));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    /**
     * Creates a styled title label.
     */
    protected JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Sans-serif", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    /**
     * Creates a main content panel with vertical box layout.
     */
    protected JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(50, 50, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        return panel;
    }
    
    /**
     * Removes spacebar activation from a button to prevent conflicts with simulation controls.
     */
    protected void removeSpacebarActivation(JButton button) {
        InputMap inputMap = button.getInputMap(JComponent.WHEN_FOCUSED);
        KeyStroke spaceKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0);
        inputMap.put(spaceKey, "none");
        
        // Also remove from ancestor input map
        InputMap ancestorMap = button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (ancestorMap != null) {
            ancestorMap.put(spaceKey, "none");
        }
    }
}

