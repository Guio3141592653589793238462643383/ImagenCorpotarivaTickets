package com.austral.back.controller;

import com.austral.back.model.Ticket;
import com.austral.back.services.ticket.TicketControllerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.austral.back.model.Comentario;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketControllerService ticketControllerService;

    public TicketController(TicketControllerService ticketControllerService) {
        this.ticketControllerService = ticketControllerService;
    }

    @PostMapping("/guardar")
    public String guardarTicket(
            @RequestParam String descripcion,
            @RequestParam String tema,
            @RequestParam String tipoDePregunta,
            @RequestParam String nombreCompleto,
            @RequestParam String correoElectronico,
            @RequestParam String numeroTelefono,
            @RequestParam(required = false) String sede,
            @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
            RedirectAttributes redirectAttributes
    ) {
        ticketControllerService.procesarCreacionTicket(
                descripcion, tema, tipoDePregunta, nombreCompleto,
                correoElectronico, numeroTelefono, sede, archivos, redirectAttributes
        );
        return "redirect:/menu";
    }

    @GetMapping("/mis-tickets")
    @ResponseBody
    public List<Ticket> obtenerMisTickets(@RequestParam String email) {
        return ticketControllerService.obtenerTicketsDelUsuario(email);
    }

    @PostMapping("/responder")
    @ResponseBody
    public ResponseEntity<?> responderTicket(
            @RequestParam Integer ticketId,
            @RequestParam String mensaje,
            @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
            Authentication authentication
    ) {
        // Verificar que el usuario es dueño del ticket
        Ticket ticket = ticketControllerService.obtenerTicketPorId(ticketId);
        String emailLogueado = authentication.getName();

        if (ticket.getUsuario() == null || !ticket.getUsuario().getEmail().equals(emailLogueado)) {
            return ResponseEntity.status(403).body("No tienes permiso para responder este ticket.");
        }

        try {
            ticketControllerService.agregarRespuestaUsuario(ticketId, mensaje, archivos);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar la respuesta: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Ticket obtenerTicketPorId(@PathVariable Integer id) {
        return ticketControllerService.obtenerTicketPorId(id);
    }

    @GetMapping("/{id}/comentarios")
    @ResponseBody
    public List<Comentario> obtenerComentariosPorTicket(@PathVariable Integer id) {
        return ticketControllerService.obtenerComentariosPorTicket(id);
    }
}
