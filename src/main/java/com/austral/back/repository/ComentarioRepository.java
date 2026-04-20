package com.austral.back.repository;

import com.austral.back.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    List<Comentario> findByTicketIdOrderByMarcaTemporalAsc(Integer ticketId);

    /** Desvincular comentarios de un usuario (poner autor_id = NULL) */
    @Modifying
    @Query("UPDATE Comentario c SET c.autor = NULL WHERE c.autor.id = :usuarioId")
    void desvincularAutor(Integer usuarioId);
}

