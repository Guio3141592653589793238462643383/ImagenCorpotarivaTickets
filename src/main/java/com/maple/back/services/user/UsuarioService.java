package com.maple.back.services.user;

import com.maple.back.model.Usuario;
import com.maple.back.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene un usuario por su email
     */
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Verifica si un usuario existe por email
     */
    public boolean existePorEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    /**
     * Guarda o actualiza un usuario
     */
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

}
