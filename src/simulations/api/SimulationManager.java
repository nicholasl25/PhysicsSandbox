package simulations.api;

import simulations.physics.PhysicsEngine;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Manages multiple physics simulations.
 * Each simulation has a unique ID and can be accessed via REST API.
 */
public class SimulationManager {
    
    private static SimulationManager instance;
    private Map<String, PhysicsEngine> simulations = new ConcurrentHashMap<>();
    
    private SimulationManager() {}
    
    public static synchronized SimulationManager getInstance() {
        if (instance == null) {
            instance = new SimulationManager();
        }
        return instance;
    }
    
    /**
     * Creates a new 3D simulation and returns its ID.
     */
    public String createSimulation3D() {
        String id = UUID.randomUUID().toString();
        PhysicsEngine engine = new PhysicsEngine(3);
        engine.start();
        simulations.put(id, engine);
        return id;
    }
    
    /**
     * Creates a new 2D simulation and returns its ID.
     */
    public String createSimulation2D() {
        String id = UUID.randomUUID().toString();
        PhysicsEngine engine = new PhysicsEngine(2);
        engine.start();
        simulations.put(id, engine);
        return id;
    }
    
    /**
     * Gets a simulation by ID.
     */
    public PhysicsEngine getSimulation(String id) {
        return simulations.get(id);
    }
    
    /**
     * Removes a simulation.
     */
    public void removeSimulation(String id) {
        PhysicsEngine engine = simulations.remove(id);
        if (engine != null) {
            engine.stop();
        }
    }
    
    /**
     * Gets all simulation IDs.
     */
    public java.util.Set<String> getAllSimulationIds() {
        return simulations.keySet();
    }
}
