package simulations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Base class for all simulation control panels.
 * Provides common styling and layout functionality.
 */
public abstract class BaseControlPanel extends JPanel {
    
    private JButton toggleButton;
    private boolean isExpanded = true;
    private JPanel contentPanel;
    private static final int EXPANDED_WIDTH = 280;
    private static final int COLLAPSED_WIDTH = 40;
    
    /**
     * Creates a new base control panel.
     */
    public BaseControlPanel() {
        setupBasePanel();
        setupToggleButton();
        setupContent();
    }
    
    /**
     * Sets up the base panel styling and layout.
     */
    private void setupBasePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50));
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
    }
    
    /**
     * Sets up the toggle button at the top of the control panel.
     */
    private void setupToggleButton() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        topPanel.setBackground(new Color(50, 50, 50));
        
        toggleButton = new JButton("→");
        toggleButton.setFont(new Font("Sans-serif", Font.BOLD, 16));
        toggleButton.setForeground(Color.WHITE);
        toggleButton.setBackground(new Color(70, 70, 70));
        toggleButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        toggleButton.setFocusPainted(false);
        toggleButton.setContentAreaFilled(true);
        toggleButton.setOpaque(true);
        toggleButton.setToolTipText("Collapse/Expand Control Panel");
        
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleCollapse();
            }
        });
        
        removeSpacebarActivation(toggleButton);
        topPanel.add(toggleButton);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Toggles the collapse/expand state of the control panel.
     */
    private void toggleCollapse() {
        isExpanded = !isExpanded;
        updateCollapseState();
    }
    
    /**
     * Updates the panel state based on expanded/collapsed status.
     */
    private void updateCollapseState() {
        if (isExpanded) {
            toggleButton.setText("→");
            setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
            if (contentPanel != null) {
                contentPanel.setVisible(true);
            }
        } else {
            toggleButton.setText("←");
            setPreferredSize(new Dimension(COLLAPSED_WIDTH, 0));
            if (contentPanel != null) {
                contentPanel.setVisible(false);
            }
        }
        revalidate();
        repaint();
    }
    
    /**
     * Gets the current expanded state.
     * 
     * @return true if expanded, false if collapsed
     */
    public boolean isExpanded() {
        return isExpanded;
    }
    
    /**
     * Sets the expanded/collapsed state.
     * 
     * @param expanded true to expand, false to collapse
     */
    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
        updateCollapseState();
    }
    
    /**
     * Sets up the panel content. Subclasses should override this to add their specific content.
     * Subclasses should add their content to the content panel returned by getContentPanel().
     */
    protected abstract void setupContent();
    
    /**
     * Gets or creates the content panel where subclasses should add their content.
     * This panel will be hidden when collapsed.
     */
    protected JPanel getContentPanel() {
        if (contentPanel == null) {
            contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(new Color(50, 50, 50));
            add(contentPanel, BorderLayout.CENTER);
        }
        return contentPanel;
    }
    
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

