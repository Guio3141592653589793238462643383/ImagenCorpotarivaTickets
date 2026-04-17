package com.maple.back.services.admin;

import com.maple.back.model.Ticket;
import com.maple.back.model.Usuario;
import com.maple.back.repository.UsuarioRepository;
import com.maple.back.services.ticket.TicketService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private final TicketService ticketService;
    private final UsuarioRepository usuarioRepository;

    public AdminDashboardService(TicketService ticketService, UsuarioRepository usuarioRepository) {
        this.ticketService = ticketService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene los datos del dashboard con valores seguros
     */
    public Map<String, Object> obtenerDatosDelDashboard() {
        List<Ticket> tickets = Optional.ofNullable(ticketService.obtenerTodosOrdenados())
                .orElse(Collections.emptyList());
        List<Usuario> usuarios = Optional.ofNullable(usuarioRepository.findAll())
                .orElse(Collections.emptyList());

        long ticketsCerrados = tickets.stream()
                .filter(t -> "CERRADO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse("")))
                .count();

        Map<LocalDate, Long> creadosPorDia = tickets.stream()
                .filter(t -> t.getMarcaTemporal() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getMarcaTemporal().toLocalDate(),
                        Collectors.counting()
                ));

        Map<LocalDate, Long> cerradosPorDia = tickets.stream()
                .filter(t -> t.getFechaCierre() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getFechaCierre().toLocalDate(),
                        Collectors.counting()
                ));

        Map<String, Long> ticketsPorPunto = tickets.stream()
                .collect(Collectors.groupingBy(
                        t -> Optional.ofNullable(
                                t.getUsuario() != null ? t.getUsuario().getPunto() : null
                        ).filter(p -> !p.isBlank()).orElse("SIN_PUNTO"),
                        Collectors.counting()
                ));

        // Siempre devolver mapas no nulos
        return Map.of(
                "totalUsuarios", usuarios.size(),
                "totalTickets", tickets.size(),
                "ticketsCerrados", ticketsCerrados,
                "creadosPorDia", !creadosPorDia.isEmpty() ? creadosPorDia : Map.of(),
                "cerradosPorDia", !cerradosPorDia.isEmpty() ? cerradosPorDia : Map.of(),
                "ticketsPorPunto", !ticketsPorPunto.isEmpty() ? ticketsPorPunto : Map.of()
        );
    }
    public Map<String, Object> obtenerDatosDelDashboard(LocalDate desde, LocalDate hasta) {

        List<Ticket> tickets = Optional.ofNullable(ticketService.obtenerTodosOrdenados())
                .orElse(Collections.emptyList());

        // FILTRO POR FECHA
        if (desde != null && hasta != null) {
            tickets = tickets.stream()
                    .filter(t -> t.getMarcaTemporal() != null)
                    .filter(t -> {
                        LocalDate fecha = t.getMarcaTemporal().toLocalDate();
                        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                    })
                    .toList();
        }

        List<Usuario> usuarios = Optional.ofNullable(usuarioRepository.findAll())
                .orElse(Collections.emptyList());

        long ticketsCerrados = tickets.stream()
                .filter(t -> "CERRADO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse("")))
                .count();

        Map<LocalDate, Long> creadosPorDia = tickets.stream()
                .filter(t -> t.getMarcaTemporal() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getMarcaTemporal().toLocalDate(),
                        Collectors.counting()
                ));

        Map<LocalDate, Long> cerradosPorDia = tickets.stream()
                .filter(t -> t.getFechaCierre() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getFechaCierre().toLocalDate(),
                        Collectors.counting()
                ));

        Map<String, Long> ticketsPorPunto = tickets.stream()
                .collect(Collectors.groupingBy(
                        t -> Optional.ofNullable(
                                t.getUsuario() != null ? t.getUsuario().getPunto() : null
                        ).filter(p -> !p.isBlank()).orElse("SIN_PUNTO"),
                        Collectors.counting()
                ));

        return Map.of(
                "totalUsuarios", usuarios.size(),
                "totalTickets", tickets.size(),
                "ticketsCerrados", ticketsCerrados,
                "creadosPorDia", !creadosPorDia.isEmpty() ? creadosPorDia : Map.of(),
                "cerradosPorDia", !cerradosPorDia.isEmpty() ? cerradosPorDia : Map.of(),
                "ticketsPorPunto", !ticketsPorPunto.isEmpty() ? ticketsPorPunto : Map.of()
        );
    }
}