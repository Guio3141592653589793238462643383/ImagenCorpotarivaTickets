package com.austral.back.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String mensaje;
    private String tipo; // NUEVO_TICKET, COMENTARIO, RESPUESTA_ADMIN
    private boolean leida = false;
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    private Rol destinatario; // ADMIN = broadcast a todos los admins

    /** Para notificaciones dirigidas a un usuario específico (null = broadcast por rol) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_usuario_id")
    private Usuario destinatarioUsuario;
}
