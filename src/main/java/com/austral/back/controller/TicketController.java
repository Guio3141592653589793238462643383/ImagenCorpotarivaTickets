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
            // ── Solicitud de material ──
            @RequestParam(required = false) String decoracion,
            @RequestParam(required = false) String producto,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(value = "productos[]", required = false) List<String> productos,
            @RequestParam(value = "cantidadesProductos[]", required = false) List<String> cantidadesProductos,
            @RequestParam(required = false) Double largo,
            @RequestParam(required = false) Double ancho,
            @RequestParam(defaultValue = "false") boolean ayudaventasImpresos,
            @RequestParam(defaultValue = "false") boolean listasDePrecios,
            @RequestParam(defaultValue = "false") boolean muestrasLentes,
            @RequestParam(defaultValue = "false") boolean regaloCorporativo,
            @RequestParam(defaultValue = "false") boolean materialCapacitaciones,
            @RequestParam(defaultValue = "false") boolean opcion6,
            @RequestParam(defaultValue = "false") boolean paniosMarcados,
            @RequestParam(defaultValue = "false") boolean libretaNotas,
            @RequestParam(defaultValue = "false") boolean reglillas,
            @RequestParam(defaultValue = "false") boolean videosUsb,
            @RequestParam(defaultValue = "false") boolean esferos,
            @RequestParam(defaultValue = "false") boolean habladores,
            @RequestParam(defaultValue = "false") boolean ViniloBannerRetablo,
            @RequestParam(defaultValue = "false") boolean Bolsas,
            @RequestParam(defaultValue = "false") boolean otros,
            @RequestParam(required = false) String otrosDetalle,
            @RequestParam(defaultValue = "false") boolean noAplicaMaterial,
            RedirectAttributes redirectAttributes
    ) {
        ticketControllerService.procesarCreacionTicket(
                descripcion, tema, tipoDePregunta, nombreCompleto,
                correoElectronico, numeroTelefono, archivos,
                decoracion, producto, cantidad, largo, ancho,
                productos, cantidadesProductos,
                ayudaventasImpresos, listasDePrecios, muestrasLentes,
                regaloCorporativo, materialCapacitaciones, opcion6,
                paniosMarcados, libretaNotas, reglillas, videosUsb,
                esferos, habladores, ViniloBannerRetablo, Bolsas, otros, otrosDetalle, noAplicaMaterial,
                redirectAttributes
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
