package com.manager.auth_service.service;

import com.manager.auth_service.dto.UserResponse;
import com.manager.auth_service.dto.CreateUserRequest;
import com.manager.auth_service.model.*; 
import com.manager.auth_service.repository.*; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; 
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Transactional 
    public String createPasswordResetToken(String email) {
        // 1. Buscar usuario
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        // 2. LIMPIEZA: Borrar token anterior si existe para este usuario
        tokenRepository.deleteByUser(user);
        tokenRepository.flush(); 

        // 3. Crear nuevo token
        String token = java.util.UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken();
        myToken.setToken(token);
        myToken.setUser(user);
        myToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(myToken);
        return token;
    }

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

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // 1. Validar: Buscar el token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));

        // 2. Verificar Expiración
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("El token ha expirado. Por favor, solicita uno nuevo.");
        }

        // 3. Actualizar: Encriptar y guardar
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. Limpiar: Borrar el token para que sea de un solo uso
        tokenRepository.delete(resetToken);
    }

}