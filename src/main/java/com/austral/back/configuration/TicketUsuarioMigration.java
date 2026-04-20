package com.austral.back.configuration;

import com.austral.back.model.Ticket;
import com.austral.back.model.Usuario;
import com.austral.back.repository.TicketRepository;
import com.austral.back.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Al arrancar la app, vincula tickets viejos (sin usuario_id)
 * con el usuario correspondiente buscando por correo electrónico.
 * Solo se ejecuta una vez por arranque y solo toca tickets huérfanos.
 */
@Component
public class TicketUsuarioMigration implements CommandLineRunner {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public TicketUsuarioMigration(TicketRepository ticketRepository,
                                  UsuarioRepository usuarioRepository) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        List<Ticket> huerfanos = ticketRepository.findByUsuarioIsNull();

        if (huerfanos.isEmpty()) return;

        int vinculados = 0;
        for (Ticket ticket : huerfanos) {
            String email = ticket.getCorreoElectronico();
            if (email == null || email.isBlank()) continue;

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                ticket.setUsuario(usuario);
                ticketRepository.save(ticket);
                vinculados++;
            }
        }

        if (vinculados > 0) {
            System.out.println("[Migration] Tickets vinculados con usuario: " + vinculados + "/" + huerfanos.size());
        }
    }
}

