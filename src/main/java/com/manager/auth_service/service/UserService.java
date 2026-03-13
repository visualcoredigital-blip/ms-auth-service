package com.manager.auth_service.service;

import com.manager.auth_service.dto.UserResponse;
import com.manager.auth_service.dto.CreateUserRequest;
import com.manager.auth_service.model.Role;
import com.manager.auth_service.model.User;
import com.manager.auth_service.model.ERole;

import com.manager.auth_service.repository.RoleRepository;
import com.manager.auth_service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors; // Agregado para el Stream API

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            ERole roleEnum = ERole.valueOf(roleName);
            Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(role);
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Error: Usuario con ID " + id + " no encontrado.");
        }
        userRepository.deleteById(id);
    }

    public UserResponse updateUser(Long id, UserResponse userDto) {
        // 1. Buscar el usuario
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Actualizar campos simples
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        
        // 3. Actualizar estado (Lombok usa setEnabled para boolean enabled)
        existingUser.setEnabled(userDto.isEnabled()); 

        // 4. Actualizar Roles (Conversión de String a ERole)
        if (userDto.getRoles() != null) {
            Set<Role> roles = userDto.getRoles().stream()
                .map(roleName -> roleRepository.findByName(ERole.valueOf(roleName))
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName)))
                .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        User savedUser = userRepository.save(existingUser);

        return convertToResponse(savedUser);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());
        
        // Convertir Set<Role> a Set<String>
        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        }
        
        return response;
    }
}