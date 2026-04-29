package com.austral.back.repository;

import com.austral.back.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer>, JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByCorreoElectronicoOrderByMarcaTemporalDesc(String correoElectronico);

    List<Ticket> findAllByOrderByMarcaTemporalDesc();

    /** Tickets huérfanos que aún no tienen usuario vinculado */
    List<Ticket> findByUsuarioIsNull();

    /** Desvincular tickets de un usuario (poner usuario_id = NULL) */
    @Modifying
    @Query("UPDATE Ticket t SET t.usuario = NULL WHERE t.usuario.id = :usuarioId")
    void desvincularUsuario(Integer usuarioId);
    Page<Ticket> findByCorreoElectronicoOrderByMarcaTemporalDesc(
            String correoElectronico,
            Pageable pageable
    );
}
