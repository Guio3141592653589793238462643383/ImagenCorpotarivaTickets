package com.maple.back.controller;

import com.maple.back.dto.NotificacionDTO;
import com.maple.back.model.Rol;
import com.maple.back.model.Usuario;
import com.maple.back.services.notificacion.NotificacionService;
import com.maple.back.services.user.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionController(NotificacionService notificacionService,
                                  UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    // --- ADMIN: broadcast por rol ---
    @GetMapping("/ADMIN")
    public List<NotificacionDTO> obtenerNotificacionesAdmin() {
        return notificacionService.obtenerUltimas(Rol.ADMIN)
                .stream().map(NotificacionDTO::new).toList();
    }

    @GetMapping("/ADMIN/no-leidas")
    public long contarNoLeidasAdmin() {
        return notificacionService.contarNoLeidas(Rol.ADMIN);
    }

    @PostMapping("/ADMIN/marcar-todas-leidas")
    public void marcarTodasLeidasAdmin() {
        notificacionService.marcarTodasLeidas(Rol.ADMIN);
    }

    // --- USER: por usuario autenticado ---
    @GetMapping("/USER")
    public List<NotificacionDTO> obtenerNotificacionesUsuario(Authentication auth) {
        Usuario usuario = usuarioService.obtenerPorEmail(auth.getName());
        return notificacionService.obtenerUltimasUsuario(usuario)
                .stream().map(NotificacionDTO::new).toList();
    }

    @GetMapping("/USER/no-leidas")
    public long contarNoLeidasUsuario(Authentication auth) {
        Usuario usuario = usuarioService.obtenerPorEmail(auth.getName());
        return notificacionService.contarNoLeidasUsuario(usuario);
    }

    @PostMapping("/USER/marcar-todas-leidas")
    public void marcarTodasLeidasUsuario(Authentication auth) {
        Usuario usuario = usuarioService.obtenerPorEmail(auth.getName());
        notificacionService.marcarTodasLeidasUsuario(usuario);
    }

    // --- Compartido ---
    @PostMapping("/marcar-leida/{id}")
    public void marcarLeida(@PathVariable Integer id) {
        notificacionService.marcarLeida(id);
    }
}
