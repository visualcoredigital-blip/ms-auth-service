package com.manager.auth_service.repository;

import com.manager.auth_service.model.ERole;
import com.manager.auth_service.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}