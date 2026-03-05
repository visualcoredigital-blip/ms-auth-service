package com.manager.auth_service.controller;

import com.manager.auth_service.dto.LoginRequest;
import com.manager.auth_service.config.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // IMPORTANTE
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // IMPORTANTE
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
// Eliminamos @CrossOrigin fijo porque ya lo configuraste globalmente en SecurityConfig
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Validar contra la DB
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );

            // 2. Extraer el rol del objeto authentication
            String role = authentication.getAuthorities().stream()
                    .map(r -> r.getAuthority())
                    .findFirst()
                    .orElse("ROLE_USER");

            // 3. Generar el token (asegúrate de que JwtUtils tenga el método actualizado)
            String token = jwtUtils.generateToken(authentication.getName(), role);

            // 4. Devolver el JSON con el token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // Error de usuario/password
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            // Cualquier otro error (ej: error de JWT_SECRET_KEY)
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno en Auth-Service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/public/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }    
}