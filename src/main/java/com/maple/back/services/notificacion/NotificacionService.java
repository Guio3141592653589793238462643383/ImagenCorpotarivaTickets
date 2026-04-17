package com.maple.back.services.notificacion;

import com.maple.back.dto.NotificacionDTO;
import com.maple.back.model.Notificacion;
import com.maple.back.model.Rol;
import com.maple.back.model.Ticket;
import com.maple.back.model.Usuario;
import com.maple.back.repository.NotificacionRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository repo;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificacionService(NotificacionRepository repo,
                               SimpMessagingTemplate messagingTemplate) {
        this.repo = repo;
        this.messagingTemplate = messagingTemplate;
    }

    /** Notificación broadcast por rol (para ADMIN) */
    public void crear(String mensaje, String tipo, Ticket ticket, Rol destinatario) {
        Notificacion n = new Notificacion();
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setLeida(false);
        n.setFecha(LocalDateTime.now());
        n.setTicket(ticket);
        n.setDestinatario(destinatario);
        repo.save(n);

        NotificacionDTO dto = new NotificacionDTO(n);
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/" + destinatario.name(), dto);
    }

    /** Notificación dirigida a un usuario específico */
    public void crearParaUsuario(String mensaje, String tipo, Ticket ticket, Usuario usuario) {
        Notificacion n = new Notificacion();
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setLeida(false);
        n.setFecha(LocalDateTime.now());
        n.setTicket(ticket);
        n.setDestinatario(Rol.USER);
        n.setDestinatarioUsuario(usuario);
        repo.save(n);

        NotificacionDTO dto = new NotificacionDTO(n);
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/user/" + usuario.getId(), dto);
    }

    // --- Queries para ADMIN (broadcast) ---
    public List<Notificacion> obtenerUltimas(Rol destinatario) {
        return repo.findTop10ByDestinatarioOrderByFechaDesc(destinatario);
    }

    public long contarNoLeidas(Rol destinatario) {
        return repo.countByLeidaFalseAndDestinatario(destinatario);
    }

    public void marcarTodasLeidas(Rol destinatario) {
        List<Notificacion> noLeidas = repo.findByLeidaFalseAndDestinatario(destinatario);
        noLeidas.forEach(n -> n.setLeida(true));
        repo.saveAll(noLeidas);
    }

    // --- Queries para USUARIO específico ---
    public List<Notificacion> obtenerUltimasUsuario(Usuario usuario) {
        return repo.findTop10ByDestinatarioUsuarioOrderByFechaDesc(usuario);
    }

    public long contarNoLeidasUsuario(Usuario usuario) {
        return repo.countByLeidaFalseAndDestinatarioUsuario(usuario);
    }

    public void marcarTodasLeidasUsuario(Usuario usuario) {
        List<Notificacion> noLeidas = repo.findByLeidaFalseAndDestinatarioUsuario(usuario);
        noLeidas.forEach(n -> n.setLeida(true));
        repo.saveAll(noLeidas);
    }

    // --- Compartido ---
    public void marcarLeida(Integer id) {
        Notificacion n = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada: " + id));
        n.setLeida(true);
        repo.save(n);
    }
}
