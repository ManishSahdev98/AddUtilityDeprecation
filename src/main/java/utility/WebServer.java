package utility;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
/**
 * Simple web server to provide UI for the Deprecation Utility
 */
public class WebServer {
    private final HttpServer server;
    private final int port;
    
    public WebServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
    }
    
    private void setupRoutes() {
        // Serve static HTML file
        server.createContext("/", new StaticFileHandler());
        
        // API endpoint for deprecation
        server.createContext("/api/deprecate", new DeprecationHandler());
        
        // Set thread pool
        server.setExecutor(Executors.newFixedThreadPool(10));
    }
    
    public void start() {
        server.start();
        System.out.println("Web server started on port " + port);
        System.out.println("Open your browser and navigate to: http://localhost:" + port);
        System.out.println("The UI will allow you to input method names and project paths for deprecation");
    }
    
    public void stop() {
        server.stop(0);
        System.out.println("Web server stopped");
    }
    
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html for root path
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            try {
                // Read the file from the classpath resources
                String resourcePath = path.substring(1);
                if (resourcePath.isEmpty()) {
                    resourcePath = "index.html";
                }
                
                // Try to load from classpath first
                InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

                if (resourceStream != null) {
                    // Read from classpath
                    String content = new String(resourceStream.readAllBytes());
                    resourceStream.close();
                    
                    String contentType = getContentType(path);
                    exchange.getResponseHeaders().add("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, content.length());
                    
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(content.getBytes());
                    }
                } else {
                    // Fallback to file system for development
                    Path filePath = Paths.get("src/main/resources").resolve(resourcePath);
                    
                    if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                        String content = Files.readString(filePath);
                        String contentType = getContentType(path);
                        
                        exchange.getResponseHeaders().add("Content-Type", contentType);
                        exchange.sendResponseHeaders(200, content.length());
                        
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(content.getBytes());
                        }
                    } else {
                        // File not found
                        String response = "File not found: " + path;
                        exchange.sendResponseHeaders(404, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    }
                }
            } catch (Exception e) {
                String response = "Error reading file: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "text/plain";
        }
    }
    
    /**
     * Handler for deprecation API requests
     */
    private static class DeprecationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }
            
            try {
                // Read request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                
                // Parse JSON (simple parsing for demo purposes)
                String methodName = extractValue(requestBody, "methodName");
                String methodSignature = extractValue(requestBody, "methodSignature");
                String projectPath = extractValue(requestBody, "projectPath");

                if (methodName == null || projectPath == null) {
                    sendErrorResponse(exchange, "Missing required parameters: methodName and projectPath");
                    return;
                }
                
                // Run deprecation utility
                DeprecationUtility utility = new DeprecationUtility(projectPath);
                DeprecationResult result = utility.deprecateMethodWithResult(methodName, methodSignature);
                
                // Send success response
                String response = result.toJson();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                
            } catch (Exception e) {
                sendErrorResponse(exchange, "Error during deprecation: " + e.getMessage());
            }
        }
        
        private String extractValue(String json, String key) {
            // Simple JSON parsing for demo purposes
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);

            return m.find() ? m.group(1) : null;
        }
        
        private void sendErrorResponse(HttpExchange exchange, String error) throws IOException {
            String response = "{\"error\":\"" + error + "\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    public static class DeprecationResult {
        private final boolean success;
        private final int filesUpdated;
        private final int methodsDeprecated;
        private final int classesDeprecated;
        private final String details;
        private final String error;
        
        public DeprecationResult(boolean success, int filesUpdated, int methodsDeprecated, 
                               int classesDeprecated, String details, String error) {
            this.success = success;
            this.filesUpdated = filesUpdated;
            this.methodsDeprecated = methodsDeprecated;
            this.classesDeprecated = classesDeprecated;
            this.details = details;
            this.error = error;
        }
        
        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"success\":").append(success).append(",");
            json.append("\"filesUpdated\":").append(filesUpdated).append(",");
            json.append("\"methodsDeprecated\":").append(methodsDeprecated).append(",");
            json.append("\"classesDeprecated\":").append(classesDeprecated).append(",");

            json.append("\"details\":\"").append(details != null ? details.replace("\"", "\\\"") : "").append("\",");

            json.append("\"error\":\"").append(error != null ? error.replace("\"", "\\\"") : "").append("\"");
            json.append("}");
            return json.toString();
        }
        
        // Getters for Jackson serialization
        public boolean isSuccess() { return success; }
        public int getFilesUpdated() { return filesUpdated; }
        public int getMethodsDeprecated() { return methodsDeprecated; }
        public int getClassesDeprecated() { return classesDeprecated; }
        public String getDetails() { return details; }
        public String getError() { return error; }
    }
    
    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
            WebServer webServer = new WebServer(port);
            webServer.start();
            
            // Keep the server running without blocking
            System.out.println("Web server is now running in the background");
            System.out.println("Access the UI at: http://localhost:" + port);
            System.out.println("To stop the server, use: pkill -f WebServer");
            
            // Add shutdown hook to gracefully stop the server
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down web server...");
                webServer.stop();
            }));
            
            // Keep the main thread alive
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error starting web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}