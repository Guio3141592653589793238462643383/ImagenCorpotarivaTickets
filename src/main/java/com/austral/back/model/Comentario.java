package com.austral.back.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comentarios")
@Data
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    /* Relación con el usuario que escribió el comentario */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    /*
     * Campo desnormalizado: se mantiene para no romper templates.
     * A futuro leer desde comentario.autor.nombre.
     */
    @Column(nullable = false)
    private String remitente;

    @Column(name = "marca_temporal")
    private LocalDateTime marcaTemporal;

    @Column(name = "tiene_imagenes")
    private Boolean tieneImagenes = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @OneToMany(mappedBy = "comentario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media;

    public boolean isEsAdmin() {
        return autor != null && autor.getRol() == Rol.ADMIN;
    }
}