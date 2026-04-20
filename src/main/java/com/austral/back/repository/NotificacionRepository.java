package com.austral.back.repository;

import com.austral.back.model.Notificacion;
import com.austral.back.model.Rol;
import com.austral.back.model.Usuario;
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
