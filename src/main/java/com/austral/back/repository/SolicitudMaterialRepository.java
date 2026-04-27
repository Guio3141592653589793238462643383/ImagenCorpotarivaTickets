package com.austral.back.repository;

import com.austral.back.model.SolicitudMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitudMaterialRepository extends JpaRepository<SolicitudMaterial, Integer> {
    Optional<SolicitudMaterial> findByTicketId(Integer ticketId);
}