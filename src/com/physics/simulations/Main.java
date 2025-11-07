package com.physics.simulations;

import javax.swing.*;

/**
 * Main entry point for the Physics Simulations application.
 * This class launches the Swing GUI with the homepage.
 */
public class Main {
    public static void main(String[] args) {
        // Swing applications should run on the Event Dispatch Thread (EDT)
        // This ensures thread-safe GUI operations
        SwingUtilities.invokeLater(() -> {
            new PhysicsSimulationsApp().setVisible(true);
        });
    }
}

