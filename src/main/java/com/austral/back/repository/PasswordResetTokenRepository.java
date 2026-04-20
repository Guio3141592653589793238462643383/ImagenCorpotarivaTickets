package com.austral.back.repository;

import com.austral.back.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Integer> {

    PasswordResetToken findByToken(String token);

    void deleteByUsuarioId(Integer usuarioId);
}