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
            String sede,
            MultipartFile[] archivos,
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
                    sede,
                    archivos
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

    // ✅ Adecuación: ahora sí retornan valores
    public Ticket obtenerTicketPorId(Integer id) {
        return ticketService.obtenerPorId(id);
    }

    public List<Comentario> obtenerComentariosPorTicket(Integer id) {
        return ticketService.obtenerComentariosPorTicket(id);
    }
}
