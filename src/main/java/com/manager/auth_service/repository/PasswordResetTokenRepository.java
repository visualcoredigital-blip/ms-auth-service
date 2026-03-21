package com.manager.auth_service.repository;

import com.manager.auth_service.model.PasswordResetToken;
import com.manager.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user); // Para limpiar tokens viejos al generar uno nuevo
}