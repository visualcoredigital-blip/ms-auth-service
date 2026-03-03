package com.manager.auth_service.repository;

import com.manager.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Este método es vital para que el CustomUserDetailsService 
    // pueda buscar al usuario durante el proceso de login.
    Optional<User> findByUsername(String username);
}