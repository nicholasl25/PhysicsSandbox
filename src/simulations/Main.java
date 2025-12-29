package simulations;

import java.io.IOException;

/**
 * Main entry point for the Physics Simulations application.
 * Starts a web server that serves the homepage and launches simulations.
 */
public class Main {
    public static void main(String[] args) {
        try {
            WebServer server = new WebServer();
            server.start();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop();
            }));
        } catch (IOException e) {
            System.err.println("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

