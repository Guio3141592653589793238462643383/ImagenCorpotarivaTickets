package com.austral.back.repository;

import com.austral.back.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Buscar usuarios paginados por coincidencia en nombre, email o cédula
    Page<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCaseOrCedulaContainingIgnoreCase(
            String nombre,
            String email,
            String cedula,
            Pageable pageable
    );
}
