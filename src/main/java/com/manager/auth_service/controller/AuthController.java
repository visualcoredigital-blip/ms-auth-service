package com.manager.auth_service.controller;

import com.manager.auth_service.dto.LoginRequest;
import com.manager.auth_service.config.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Permite que el Frontend acceda a este controlador
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Validar contra la DB (Spring Security hace el trabajo sucio)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            )
        );

        // 2. Extraer el rol del objeto authentication
        // Tomamos el primero de la lista (asumiendo que tiene uno principal como ROLE_USER o ROLE_ADMIN)
        String role = authentication.getAuthorities().stream()
                .map(r -> r.getAuthority())
                .findFirst()
                .orElse("ROLE_USER");

        // 3. Generar el token incluyendo el rol (usando el cambio que hicimos en JwtUtils)
        String token = jwtUtils.generateToken(authentication.getName(), role);

        // 4. Devolver el JSON con el token
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }

}