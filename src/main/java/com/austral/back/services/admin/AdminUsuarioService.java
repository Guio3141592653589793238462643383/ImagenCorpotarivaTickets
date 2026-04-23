package com.austral.back.services.admin;

import com.austral.back.model.Usuario;
import com.austral.back.repository.ComentarioRepository;
import com.austral.back.repository.PasswordResetTokenRepository;
import com.austral.back.repository.TicketRepository;
import com.austral.back.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final ComentarioRepository comentarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public AdminUsuarioService(UsuarioRepository usuarioRepository,
                               TicketRepository ticketRepository,
                               ComentarioRepository comentarioRepository,
                               PasswordResetTokenRepository passwordResetTokenRepository,
                               PasswordEncoder passwordEncoder,
                               FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.comentarioRepository = comentarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Obtiene usuarios con búsqueda y paginación
     */
    public Page<Usuario> obtenerUsuariosConBusqueda(String buscar, Pageable pageable) {
        if (buscar != null && !buscar.isBlank()) {
            return usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    buscar, buscar, pageable);
        }
        return usuarioRepository.findAll(pageable);
    }

    /**
     * Obtiene un usuario por ID
     */
    public Usuario obtenerPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Guarda un usuario nuevo o actualiza uno existente
     */
    public void guardarUsuario(Usuario usuario) {
        if (usuario.getId() != null) {
            // Actualizar usuario existente
            Usuario existente = obtenerPorId(usuario.getId());

            // Si no escriben nueva contraseña → mantener la anterior
            if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
                usuario.setContrasena(existente.getContrasena());
            } else {
                usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
            }
        } else {
            // Nuevo usuario → siempre encriptar
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Elimina un usuario: mata sus sesiones, desvincula datos y lo borra
     */
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = obtenerPorId(id);

        // 0. Invalidar todas las sesiones activas del usuario (kick out)
        Map<String, ? extends Session> sessions =
                sessionRepository.findByPrincipalName(usuario.getEmail());
        sessions.values().forEach(session ->
                sessionRepository.deleteById(session.getId()));

        // 1. Desvincular tickets (poner usuario_id = NULL, los tickets se mantienen)
        ticketRepository.desvincularUsuario(id);

        // 2. Desvincular comentarios (poner autor_id = NULL)
        comentarioRepository.desvincularAutor(id);

        // 3. Borrar tokens de reset de password
        passwordResetTokenRepository.deleteByUsuarioId(id);

        // 4. Ahora sí borrar el usuario
        usuarioRepository.deleteById(id);
    }

    /**
     * Obtiene todos los usuarios
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
}

