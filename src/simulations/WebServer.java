package simulations;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServer {
    
    private static final int PORT = 8080;
    private HttpServer server;
    
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/launch/gravity2d", new LaunchGravity2DHandler());
        server.createContext("/api/launch/schwarzschild", new LaunchSchwarzschildHandler());
        
        // REST API endpoints for web-based simulations
        server.createContext("/api/simulations", new SimulationAPIHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Web server started on http://localhost:" + PORT);
        System.out.println("Open your browser and navigate to the URL above");
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
    
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            Path filePath = null;
            
            // Check if it's a texture file
            if (path.startsWith("/textures/")) {
                filePath = Paths.get("resources" + path);
            } else {
                // Otherwise, serve from web directory
                filePath = Paths.get("resources/web" + path);
            }
            
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                send404(exchange);
                return;
            }
            
            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = getContentType(path);
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "application/octet-stream";
        }
        
        private void send404(HttpExchange exchange) throws IOException {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    private static class LaunchGravity2DHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            try {
                SwingUtilities.invokeLater(() -> {
                    try {
                        simulations.NewtonianGravity.Gravity2D.Gravity2DSimulation sim = 
                            new simulations.NewtonianGravity.Gravity2D.Gravity2DSimulation();
                        sim.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                sendResponse(exchange, 200, "OK");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    private static class LaunchSchwarzschildHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            try {
                SwingUtilities.invokeLater(() -> {
                    try {
                        simulations.Schwarzchild.SchwarzchildSimulation sim = 
                            new simulations.Schwarzchild.SchwarzchildSimulation();
                        sim.initialize();
                        sim.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                sendResponse(exchange, 200, "OK");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    private static void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
        exchange.sendResponseHeaders(code, message.length());
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }
    
    private static void sendError(HttpExchange exchange, int code, String error) throws IOException {
        sendResponse(exchange, code, error);
    }
    
    // Handler for REST API endpoints - routes based on path pattern
    private static class SimulationAPIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            
            // POST /api/simulations - create new simulation
            if (path.equals("/api/simulations") && method.equals("POST")) {
                new simulations.api.SimulationAPI.CreateSimulationHandler().handle(exchange);
                return;
            }
            
            // Extract simulation ID from path: /api/simulations/{id}/...
            String[] parts = path.split("/");
            if (parts.length < 4 || !parts[1].equals("api") || !parts[2].equals("simulations")) {
                sendError(exchange, 404, "Not found");
                return;
            }
            
            String id = parts[3];
            String subPath = parts.length > 4 ? parts[4] : "";
            
            // GET /api/simulations/{id}/state
            if (subPath.equals("state") && method.equals("GET")) {
                new simulations.api.SimulationAPI.GetStateHandler().handle(exchange);
            }
            // POST /api/simulations/{id}/planets
            else if (subPath.equals("planets") && method.equals("POST")) {
                new simulations.api.SimulationAPI.AddPlanetHandler().handle(exchange);
            }
            // DELETE /api/simulations/{id}/planets
            else if (subPath.equals("planets") && method.equals("DELETE")) {
                new simulations.api.SimulationAPI.ClearPlanetsHandler().handle(exchange);
            }
            // PUT /api/simulations/{id}/settings
            else if (subPath.equals("settings") && method.equals("PUT")) {
                new simulations.api.SimulationAPI.UpdateSettingsHandler().handle(exchange);
            }
            else {
                sendError(exchange, 404, "Not found");
            }
        }
    }
}

