package au.com.equifax.cicddemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador demo para el taller de CI/CD.
 * Contiene deuda técnica intencional para que SonarQube la detecte.
 */
@RestController
public class DemoController {

    // Security Hotspot: credenciales hardcodeadas (detectado por SonarQube)
    private static final String DB_PASSWORD = "admin123";
    private static final String API_KEY = "secret-key-12345";

    private static final String APP_MESSAGE = "Hola desde el pipeline de CI/CD - v2.0";

    @GetMapping("/demo")
    public Map<String, String> demo() {
        Map<String, String> response = new HashMap<>();
        response.put("message", APP_MESSAGE);
        response.put("status", "running");
        response.put("version", "2.0");
        return response;
    }

    // Code smell: parámetro sin sanitizar (detectado por SonarQube como vulnerability)
    @GetMapping("/search")
    public Map<String, String> search(@RequestParam String query) {
        Map<String, String> response = new HashMap<>();
        // Vulnerabilidad intencional: interpolación directa sin escapar (SQL Injection pattern)
        String sql = "SELECT * FROM users WHERE name = '" + query + "'";
        response.put("query_executed", sql);
        response.put("result", "Búsqueda completada para: " + query);
        return response;
    }

    // Dead code: método nunca usado (detectado por SonarQube)
    private void unusedMethod() {
        System.out.println("Este método nunca se llama");
        String unused = "variable sin usar";
    }
}
