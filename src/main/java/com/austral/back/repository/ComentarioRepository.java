package com.austral.back.repository;

import com.austral.back.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    @Query("""
        SELECT DISTINCT c FROM Comentario c
        LEFT JOIN FETCH c.media
        LEFT JOIN FETCH c.autor
        WHERE c.ticket.id = :ticketId
        ORDER BY c.marcaTemporal ASC
    """)
    List<Comentario> findByTicketIdOrderByMarcaTemporalAsc(@Param("ticketId") Integer ticketId);

    @Modifying
    @Query("UPDATE Comentario c SET c.autor = NULL WHERE c.autor.id = :usuarioId")
    void desvincularAutor(Integer usuarioId);
}