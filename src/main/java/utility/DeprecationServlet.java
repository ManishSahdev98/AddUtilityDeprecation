package utility;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


public class DeprecationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;

                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            
            JsonNode jsonNode = objectMapper.readTree(requestBody.toString());

            String methodName = jsonNode.has("methodName") ? jsonNode.get("methodName").asText() : null;

            String methodSignature = jsonNode.has("methodSignature") ? jsonNode.get("methodSignature").asText() : null;

            String projectPath = jsonNode.has("projectPath") ? jsonNode.get("projectPath").asText() : null;

            if (methodName == null || projectPath == null) {
                sendErrorResponse(response, "Missing required parameters: methodName and projectPath");
                return;
            }
            DeprecationUtility utility = new DeprecationUtility(projectPath);
            WebServer.DeprecationResult result = utility.deprecateMethodWithResult(methodName, methodSignature);
            
            String jsonResponse = objectMapper.writeValueAsString(result);
            response.setStatus(HttpServletResponse.SC_OK);
            
            try (PrintWriter out = response.getWriter()) {
                out.print(jsonResponse);
            }
            
        } catch (Exception e) {
            sendErrorResponse(response, "Error during deprecation: " + e.getMessage());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("application/json");
        
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"error\":\"GET method not allowed. Use POST.\"}");
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, String error) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"error\":\"" + error + "\"}");
        }
    }
}