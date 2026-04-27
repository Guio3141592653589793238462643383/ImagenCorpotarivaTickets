package com.austral.back.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "comentarios", "mediaList", "solicitudMaterial"})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "marca_temporal")
    private LocalDateTime marcaTemporal;

    /* ── Relación con el usuario ── */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "correo_electronico", nullable = false)
    private String correoElectronico;

    @Column(name = "numero_telefono", nullable = false)
    private String numeroTelefono;

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

    /* ── Solicitud de material ── */
    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private SolicitudMaterial solicitudMaterial;

    /* ── Helpers ── */

    public String getNombreActual() {
        return usuario != null ? usuario.getNombre() : nombreCompleto;
    }

    public String getEmailActual() {
        return usuario != null ? usuario.getEmail() : correoElectronico;
    }

    public String getTelefonoActual() {
        return usuario != null ? usuario.getTelefono() : numeroTelefono;
    }

    public String getCargoActual() {
        return usuario != null ? usuario.getCargo() : "";
    }

    public String getPuntoActual() {
        return usuario != null ? usuario.getPunto() : "";
    }
}