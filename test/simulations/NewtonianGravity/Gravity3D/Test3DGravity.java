package simulations.NewtonianGravity.Gravity3D;

import javax.swing.SwingUtilities;

/**
 * Test class to directly launch the 3D gravity simulation
 * without needing the web server.
 */
public class Test3DGravity {
    
    public static void main(String[] args) {
        System.out.println("Testing 3D Gravity Simulation...\n");
        System.out.println("Launching simulation window...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                Gravity3DSimulation sim = new Gravity3DSimulation();
                sim.start();
                System.out.println("Simulation started successfully");
            } catch (Exception e) {
                System.err.println("ERROR: Failed to start simulation");
                e.printStackTrace();
                System.exit(1);
            }
        });
        
        // Keep the main thread alive
        try {
            Thread.sleep(1000);
            System.out.println("Simulation should be running now. Check the window.");
            System.out.println("Press Ctrl+C to exit.");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Exiting...");
        }
    }
}
