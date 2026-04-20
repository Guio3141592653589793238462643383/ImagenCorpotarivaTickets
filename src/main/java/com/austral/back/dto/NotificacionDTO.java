package com.austral.back.dto;

import com.austral.back.model.Notificacion;
import com.austral.back.model.Rol;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificacionDTO {
    private Integer id;
    private String mensaje;
    private String tipo;
    private boolean leida;
    private LocalDateTime fecha;
    private Integer ticketId;
    private Rol destinatario;
    private Integer destinatarioUsuarioId;

    public NotificacionDTO(Notificacion n) {
        this.id = n.getId();
        this.mensaje = n.getMensaje();
        this.tipo = n.getTipo();
        this.leida = n.isLeida();
        this.fecha = n.getFecha();
        this.ticketId = n.getTicket() != null ? n.getTicket().getId() : null;
        this.destinatario = n.getDestinatario();
        this.destinatarioUsuarioId = n.getDestinatarioUsuario() != null
                ? n.getDestinatarioUsuario().getId() : null;
    }
}
