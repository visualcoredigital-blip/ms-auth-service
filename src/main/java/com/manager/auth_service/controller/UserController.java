package com.manager.auth_service.controller;

import com.manager.auth_service.dto.CreateUserRequest;
import com.manager.auth_service.dto.UserResponse;
import com.manager.auth_service.model.User;
import com.manager.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.manager.auth_service.dto.ResetPasswordDTO;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        try {
            // Llamamos al servicio para crear el token en la tabla independiente
            String token = userService.createPasswordResetToken(email);
            
            // Preparamos la respuesta que el Manager leerá
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Token de recuperación generado exitosamente.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Si el email no existe, devolvemos 404
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al generar el token");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al actualizar la contraseña"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // Asumiendo que tienes este método en tu service
        return ResponseEntity.ok(userService.findAll()); 
    }

    // POST /api/users -> Para crear usuarios
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateUserRequest request) {
        System.out.println("¡Entré al controlador de creación!");
        User user = userService.createUser(request);
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());
        response.setRoles(
            user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet())
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        System.out.println("🚀 eliminar usuario con ID: " + id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al eliminar el usuario");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserResponse userDto) {
        UserResponse updated = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updated);
    }    
}