package com.austral.back.repository;

import com.austral.back.model.TicketMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMediaRepository extends JpaRepository<TicketMedia, Integer> {
    /**
     * Obtiene todos los medios de un ticket
     */
    List<TicketMedia> findByTicketId(Integer ticketId);
}
