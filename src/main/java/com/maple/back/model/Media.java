package com.maple.back.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "media")
@Data
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "media_path", nullable = false)
    private String mediaPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Comentario comentario;
}
