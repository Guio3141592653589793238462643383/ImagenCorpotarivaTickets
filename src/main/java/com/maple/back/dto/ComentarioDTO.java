package com.maple.back.dto;

import com.maple.back.model.Comentario;
import com.maple.back.model.Media;
import com.maple.back.model.Rol;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ComentarioDTO {
    private Integer id;
    private String mensaje;
    private String remitente;
    private boolean esAdmin;
    private LocalDateTime marcaTemporal;
    private Boolean tieneImagenes;
    private Integer ticketId;
    private List<String> mediaPaths;

    public ComentarioDTO(Comentario c) {
        this.id = c.getId();
        this.mensaje = c.getMensaje();
        this.remitente = c.getRemitente();
        this.marcaTemporal = c.getMarcaTemporal();
        this.tieneImagenes = c.getTieneImagenes();
        this.ticketId = c.getTicket() != null ? c.getTicket().getId() : null;
        this.mediaPaths = c.getMedia() != null
                ? c.getMedia().stream().map(Media::getMediaPath).toList()
                : List.of();

        // Determinar si el comentario es de un admin
        if (c.getAutor() != null) {
            this.esAdmin = c.getAutor().getRol() == Rol.ADMIN;
        } else {
            // Fallback: si no hay autor vinculado, chequear si el remitente
            // no coincide con el nombre del usuario del ticket
            this.esAdmin = c.getTicket() != null
                    && c.getTicket().getUsuario() != null
                    && !c.getRemitente().equals(c.getTicket().getUsuario().getNombre());
        }
    }
}

