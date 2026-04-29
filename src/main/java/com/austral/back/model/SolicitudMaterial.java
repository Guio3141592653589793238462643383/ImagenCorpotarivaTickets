package com.austral.back.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes_material")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"ticket", "productos", "materiales"})
public class SolicitudMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /* ── Relación con el ticket ── */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /* ── De una opción ── */
    private String decoracion;
    private String producto;
    private Integer cantidad;
    private Double largo;
    private Double ancho;

    /* ── Checkboxes ── */
    private boolean ayudaventasImpresos;
    private boolean listasDePrecios;
    private boolean muestrasLentes;
    private boolean regaloCorporativo;
    private boolean materialCapacitaciones;
    private boolean opcion6;
    private boolean paniosMarcados;
    private boolean libretaNotas;
    private boolean reglillas;
    private boolean videosUsb;
    private boolean esferos;
    private boolean habladores;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitudProductoItem> productos = new ArrayList<>();

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitudMaterialItem> materiales = new ArrayList<>();
}
