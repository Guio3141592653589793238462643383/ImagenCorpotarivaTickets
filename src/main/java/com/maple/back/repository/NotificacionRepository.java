package com.maple.back.repository;

import com.maple.back.model.Notificacion;
import com.maple.back.model.Rol;
import com.maple.back.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // --- Para ADMIN (broadcast por rol) ---
    List<Notificacion> findTop10ByDestinatarioOrderByFechaDesc(Rol destinatario);
    long countByLeidaFalseAndDestinatario(Rol destinatario);
    List<Notificacion> findByLeidaFalseAndDestinatario(Rol destinatario);

    // --- Para USUARIO específico ---
    List<Notificacion> findTop10ByDestinatarioUsuarioOrderByFechaDesc(Usuario usuario);
    long countByLeidaFalseAndDestinatarioUsuario(Usuario usuario);
    List<Notificacion> findByLeidaFalseAndDestinatarioUsuario(Usuario usuario);
}
