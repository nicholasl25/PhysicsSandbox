package simulations.api;

import simulations.physics.PhysicsEngine;
import simulations.NewtonianGravity.Planet;
import simulations.NewtonianGravity.PointMass;
import simulations.NewtonianGravity.Vector;
import java.awt.Color;
import java.io.*;
import java.util.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * REST API endpoints for simulation control.
 * Handles JSON requests/responses for managing simulations.
 */
public class SimulationAPI {
    
    private static SimulationManager manager = SimulationManager.getInstance();
    
    /**
     * Handler for getting simulation state.
     * GET /api/simulations/{id}/state
     */
    public static class GetStateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCORS(exchange)) return;
            if (!exchange.getRequestMethod().equals("GET")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String id = extractSimulationId(path);
            
            if (id == null) {
                sendError(exchange, 400, "Invalid simulation ID");
                return;
            }
            
            PhysicsEngine engine = manager.getSimulation(id);
            if (engine == null) {
                sendError(exchange, 404, "Simulation not found");
                return;
            }
            
            String state = buildStateJSON(engine);
            sendJSON(exchange, 200, state);
        }
    }
    
    /**
     * Handler for adding a planet.
     * POST /api/simulations/{id}/planets
     */
    public static class AddPlanetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCORS(exchange)) return;
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String id = extractSimulationId(path);
            
            if (id == null) {
                sendError(exchange, 400, "Invalid simulation ID");
                return;
            }
            
            PhysicsEngine engine = manager.getSimulation(id);
            if (engine == null) {
                sendError(exchange, 404, "Simulation not found");
                return;
            }
            
            try {
                String requestBody = readRequestBody(exchange);
                Planet planet = parsePlanetFromJSON(requestBody, engine.getDimension());
                engine.addPlanet(planet);
                
                sendJSON(exchange, 200, "{\"status\":\"ok\"}");
            } catch (Exception e) {
                sendError(exchange, 400, "Invalid request: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler for clearing all planets.
     * DELETE /api/simulations/{id}/planets
     */
    public static class ClearPlanetsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCORS(exchange)) return;
            if (!exchange.getRequestMethod().equals("DELETE")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String id = extractSimulationId(path);
            
            if (id == null) {
                sendError(exchange, 400, "Invalid simulation ID");
                return;
            }
            
            PhysicsEngine engine = manager.getSimulation(id);
            if (engine == null) {
                sendError(exchange, 404, "Simulation not found");
                return;
            }
            
            engine.clearPlanets();
            sendJSON(exchange, 200, "{\"status\":\"ok\"}");
        }
    }
    
    /**
     * Handler for updating simulation settings.
     * PUT /api/simulations/{id}/settings
     */
    public static class UpdateSettingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCORS(exchange)) return;
            if (!exchange.getRequestMethod().equals("PUT")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String id = extractSimulationId(path);
            
            if (id == null) {
                sendError(exchange, 400, "Invalid simulation ID");
                return;
            }
            
            PhysicsEngine engine = manager.getSimulation(id);
            if (engine == null) {
                sendError(exchange, 404, "Simulation not found");
                return;
            }
            
            try {
                String requestBody = readRequestBody(exchange);
                Map<String, String> json = SimpleJSONParser.parse(requestBody);
                
                if (json.containsKey("gravitationalConstant")) {
                    engine.setGravitationalConstant(SimpleJSONParser.getDouble(json, "gravitationalConstant", 6000.0));
                }
                if (json.containsKey("timeFactor")) {
                    engine.setTimeFactor(SimpleJSONParser.getDouble(json, "timeFactor", 1.0));
                }
                if (json.containsKey("bounce")) {
                    engine.setBounce(SimpleJSONParser.getBoolean(json, "bounce", false));
                }
                if (json.containsKey("coefficientOfRestitution")) {
                    engine.setCoefficientOfRestitution(SimpleJSONParser.getDouble(json, "coefficientOfRestitution", 1.0));
                }
                if (json.containsKey("paused")) {
                    engine.setPaused(SimpleJSONParser.getBoolean(json, "paused", false));
                }
                
                sendJSON(exchange, 200, "{\"status\":\"ok\"}");
            } catch (Exception e) {
                sendError(exchange, 400, "Invalid request: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler for creating a new simulation.
     * POST /api/simulations
     */
    public static class CreateSimulationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCORS(exchange)) return;
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            try {
                String requestBody = readRequestBody(exchange);
                Map<String, String> json = SimpleJSONParser.parse(requestBody);
                String dimension = SimpleJSONParser.getString(json, "dimension", "3");
                
                String id;
                if (dimension.equals("2")) {
                    id = manager.createSimulation2D();
                } else {
                    id = manager.createSimulation3D();
                }
                
                String response = "{\"id\":\"" + id + "\",\"dimension\":\"" + dimension + "\"}";
                sendJSON(exchange, 201, response);
            } catch (Exception e) {
                sendError(exchange, 400, "Invalid request: " + e.getMessage());
            }
        }
    }
    
    // Helper methods
    
    private static String extractSimulationId(String path) {
        // Path format: /api/simulations/{id}/...
        String[] parts = path.split("/");
        if (parts.length >= 4 && parts[1].equals("api") && parts[2].equals("simulations")) {
            return parts[3];
        }
        return null;
    }
    
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    private static Planet parsePlanetFromJSON(String jsonStr, int dimension) throws Exception {
        // Simple JSON parsing - extract nested objects manually
        // This is a simplified parser for the planet JSON structure
        double mass = extractDouble(jsonStr, "mass");
        double radius = extractDouble(jsonStr, "radius");
        
        double[] posArray = new double[dimension];
        posArray[0] = extractDouble(jsonStr, "\"position\":{\"x\":");
        posArray[1] = extractDouble(jsonStr, "\"y\":");
        if (dimension == 3) {
            posArray[2] = extractDouble(jsonStr, "\"z\":");
        }
        Vector pos = new Vector(posArray);
        
        double[] velArray = new double[dimension];
        velArray[0] = extractDouble(jsonStr, "\"velocity\":{\"x\":");
        velArray[1] = extractDouble(jsonStr, "\"y\":");
        if (dimension == 3) {
            velArray[2] = extractDouble(jsonStr, "\"z\":");
        }
        Vector vel = new Vector(velArray);
        
        double angularVelocity = extractDouble(jsonStr, "angularVelocity", 0.0);
        double temperature = extractDouble(jsonStr, "temperature", 0.0);
        
        int r = (int)extractDouble(jsonStr, "\"color\":{\"r\":", 255);
        int g = (int)extractDouble(jsonStr, "\"g\":", 255);
        int b = (int)extractDouble(jsonStr, "\"b\":", 255);
        Color color = new Color(r, g, b);
        
        String texturePath = extractString(jsonStr, "texture");
        String name = extractString(jsonStr, "name");
        boolean fixedLocation = jsonStr.contains("\"fixedLocation\":true");
        
        if (fixedLocation) {
            return new PointMass(mass, radius, posArray[0], posArray[1], 
                               angularVelocity, temperature, color, texturePath, name);
        } else {
            return new Planet(dimension, mass, radius, pos, vel, 
                            angularVelocity, temperature, color, texturePath, name);
        }
    }
    
    private static double extractDouble(String json, String key) throws Exception {
        return extractDouble(json, "\"" + key + "\":", 0.0);
    }
    
    private static double extractDouble(String json, String pattern, double defaultValue) {
        int idx = json.indexOf(pattern);
        if (idx == -1) return defaultValue;
        idx += pattern.length();
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-' || json.charAt(end) == 'e' || json.charAt(end) == 'E' || json.charAt(end) == '+')) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(idx, end).trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        idx += pattern.length();
        int end = json.indexOf("\"", idx);
        if (end == -1) return null;
        return json.substring(idx, end);
    }
    
    private static String buildStateJSON(PhysicsEngine engine) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"settings\":{");
        sb.append("\"gravitationalConstant\":").append(engine.getGravitationalConstant()).append(",");
        sb.append("\"timeFactor\":").append(engine.getTimeFactor()).append(",");
        sb.append("\"bounce\":").append(engine.isBounce()).append(",");
        sb.append("\"coefficientOfRestitution\":").append(engine.getCoefficientOfRestitution()).append(",");
        sb.append("\"paused\":").append(engine.isPaused());
        sb.append("},\"planets\":[");
        
        List<Planet> planets = engine.getPlanets();
        boolean first = true;
        for (Planet planet : planets) {
            if (!first) sb.append(",");
            sb.append(planetToJSON(planet));
            first = false;
        }
        
        sb.append("]}");
        return sb.toString();
    }
    
    private static String planetToJSON(Planet planet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        Vector pos = planet.getPosition();
        double[] posArray = pos.getData();
        sb.append("\"position\":{\"x\":").append(posArray[0]);
        sb.append(",\"y\":").append(posArray[1]);
        if (posArray.length > 2) {
            sb.append(",\"z\":").append(posArray[2]);
        }
        sb.append("},");
        
        Vector vel = planet.getVelocity();
        double[] velArray = vel.getData();
        sb.append("\"velocity\":{\"x\":").append(velArray[0]);
        sb.append(",\"y\":").append(velArray[1]);
        if (velArray.length > 2) {
            sb.append(",\"z\":").append(velArray[2]);
        }
        sb.append("},");
        
        sb.append("\"mass\":").append(planet.getMass()).append(",");
        sb.append("\"radius\":").append(planet.getRadius()).append(",");
        sb.append("\"angularVelocity\":").append(planet.getAngularVelocity()).append(",");
        sb.append("\"rotationAngle\":").append(planet.getRotationAngle()).append(",");
        sb.append("\"temperature\":").append(planet.getTemperature()).append(",");
        sb.append("\"name\":\"").append(planet.getName() != null ? planet.getName() : "").append("\",");
        
        Color color = planet.getColor();
        sb.append("\"color\":{\"r\":").append(color.getRed());
        sb.append(",\"g\":").append(color.getGreen());
        sb.append(",\"b\":").append(color.getBlue());
        sb.append("},");
        
        sb.append("\"fixedLocation\":").append(planet instanceof PointMass);
        sb.append("}");
        
        return sb.toString();
    }
    
    private static void sendJSON(HttpExchange exchange, int code, String json) throws IOException {
        setCORSHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] response = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
    
    private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String error = "{\"error\":\"" + SimpleJSON.escape(message) + "\"}";
        sendJSON(exchange, code, error);
    }
    
    private static void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
    
    /**
     * Handle OPTIONS request for CORS preflight.
     */
    private static boolean handleCORS(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            setCORSHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }
}
