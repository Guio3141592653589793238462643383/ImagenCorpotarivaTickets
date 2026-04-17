package com.maple.back.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tickets")
@Data
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "marca_temporal")
    private LocalDateTime marcaTemporal;

    /* ── Relación con el usuario que creó el ticket ── */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /*
     * Campos desnormalizados: se mantienen por ahora para no romper
     * templates / reportes / emails que los leen directamente.
     * A futuro se pueden eliminar y leer desde ticket.usuario.
     */
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "correo_electronico", nullable = false)
    private String correoElectronico;

    @Column(name = "numero_telefono", nullable = false)
    private String numeroTelefono;

    private String ciudad;

    /* ── Datos del ticket ── */
    @Column(name = "tipo_de_pregunta", nullable = false)
    private String tipoDePregunta;

    @Column(nullable = false)
    private String tema;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String priority = "Baja";

    @Column(name = "atendido_por")
    private String atendidoPor;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    /* ── Relaciones ── */
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketMedia> mediaList;

    /* ── Helpers: siempre devuelven dato actualizado del usuario ── */

    /** Nombre actual del usuario (si existe), si no el que se guardó al crear */
    public String getNombreActual() {
        return usuario != null ? usuario.getNombre() : nombreCompleto;
    }

    /** Email actual del usuario (si existe), si no el que se guardó al crear */
    public String getEmailActual() {
        return usuario != null ? usuario.getEmail() : correoElectronico;
    }

    /** Teléfono actual del usuario (si existe), si no el que se guardó al crear */
    public String getTelefonoActual() {
        return usuario != null ? usuario.getTelefono() : numeroTelefono;
    }

    /** Ciudad actual del usuario (si existe), si no la que se guardó al crear */
    public String getCiudadActual() {
        return usuario != null ? usuario.getCiudad() : ciudad;
    }

    public String getCargoActual() {
        return usuario != null ? usuario.getCargo() : "";
    }

    public String getPuntoActual() {
        return usuario != null ? usuario.getPunto() : "";
    }

}