package com.manager.auth_service.service;

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
}