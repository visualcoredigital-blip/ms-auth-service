package com.manager.auth_service.controller;

import com.manager.auth_service.dto.CreateUserRequest;
import com.manager.auth_service.dto.UserResponse;
import com.manager.auth_service.model.User;
import com.manager.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // GET /api/users -> El que llama el Manager
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // Asumiendo que tienes este método en tu service
        return ResponseEntity.ok(userService.findAll()); 
    }

    // POST /api/users -> Para crear usuarios
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
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
}