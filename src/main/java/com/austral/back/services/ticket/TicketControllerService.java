package com.austral.back.services.ticket;

import com.austral.back.model.Comentario;
import com.austral.back.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Service
public class TicketControllerService {

    private final TicketService ticketService;

    public TicketControllerService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public boolean procesarCreacionTicket(
            String descripcion,
            String tema,
            String tipoDePregunta,
            String nombreCompleto,
            String correoElectronico,
            String numeroTelefono,
            MultipartFile[] archivos,
            // ── Solicitud de material ──
            String decoracion,
            String producto,
            Integer cantidad,
            Double largo,
            Double ancho,
            boolean ayudaventasImpresos,
            boolean listasDePrecios,
            boolean muestrasLentes,
            boolean regaloCorporativo,
            boolean materialCapacitaciones,
            boolean opcion6,
            boolean paniosMarcados,
            boolean libretaNotas,
            boolean reglillas,
            boolean videosUsb,
            boolean esferos,
            boolean habladores,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ticketService.crearTicket(
                    descripcion,
                    tema,
                    tipoDePregunta,
                    nombreCompleto,
                    correoElectronico,
                    numeroTelefono,
                    archivos,
                    decoracion,
                    producto,
                    cantidad,
                    largo,
                    ancho,
                    ayudaventasImpresos,
                    listasDePrecios,
                    muestrasLentes,
                    regaloCorporativo,
                    materialCapacitaciones,
                    opcion6,
                    paniosMarcados,
                    libretaNotas,
                    reglillas,
                    videosUsb,
                    esferos,
                    habladores
            );
            redirectAttributes.addFlashAttribute("mensaje", "Ticket creado con éxito");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear el ticket");
            return false;
        }
    }

    public List<Ticket> obtenerTicketsDelUsuario(String email) {
        return ticketService.obtenerTicketsUsuario(email);
    }

    public void agregarRespuestaUsuario(Integer ticketId, String mensaje, MultipartFile[] archivos) throws Exception {
        ticketService.agregarRespuestaUsuario(ticketId, mensaje, archivos);
    }

    public Ticket obtenerTicketPorId(Integer id) {
        return ticketService.obtenerPorId(id);
    }

    public List<Comentario> obtenerComentariosPorTicket(Integer id) {
        return ticketService.obtenerComentariosPorTicket(id);
    }
}