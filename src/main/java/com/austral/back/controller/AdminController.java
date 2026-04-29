package com.austral.back.controller;

import com.austral.back.model.Ticket;
import com.austral.back.model.Usuario;
import com.austral.back.services.ticket.TicketService;
import com.austral.back.services.media.MediaService;
import com.austral.back.services.admin.AdminDashboardService;
import com.austral.back.services.admin.AdminReportService;
import com.austral.back.services.admin.AdminUsuarioService;
import com.austral.back.services.user.UsuarioService;
import com.austral.back.services.notificacion.NotificacionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.austral.back.model.Rol;
import com.austral.back.services.email.EmailService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.util.Map;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TicketService ticketService;
    private final AdminDashboardService adminDashboardService;
    private final AdminReportService adminReportService;
    private final EmailService emailService;
    private final AdminUsuarioService adminUsuarioService;
    private final MediaService mediaService;
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public AdminController(TicketService ticketService,
                           AdminDashboardService adminDashboardService,
                           AdminReportService adminReportService,
                           AdminUsuarioService adminUsuarioService,
                           MediaService mediaService,
                           EmailService emailService,
                           UsuarioService usuarioService,
                           NotificacionService notificacionService
                           ) {
        this.ticketService = ticketService;
        this.adminDashboardService = adminDashboardService;
        this.adminReportService = adminReportService;
        this.adminUsuarioService = adminUsuarioService;
        this.mediaService = mediaService;
        this.emailService = emailService;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // ENDPOINT PARA DATOS
    @GetMapping("/dashboard/data")
    @ResponseBody
    public Map<String, Object> dashboardData(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta
    ) {

        if (desde != null && hasta != null) {
            return adminDashboardService.obtenerDatosDelDashboard(desde, hasta);
        }

        return adminDashboardService.obtenerDatosDelDashboard();
    }
    // USUARIOS
    @GetMapping("/usuarios")
    public String usuarios(
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        page = Math.max(page, 0);
        size = normalizarSize(size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Usuario> paginaUsuarios = adminUsuarioService.obtenerUsuariosConBusqueda(buscar, pageable);

        model.addAttribute("usuarios", paginaUsuarios.getContent());
        model.addAttribute("pagina", paginaUsuarios);
        model.addAttribute("buscar", buscar);
        model.addAttribute("size", size);
        model.addAttribute("totalTickets", ticketService.obtenerTodosOrdenados().size());

        return "admin/usuarios";
    }

    @GetMapping("/dashboard/reporte")
    public void exportarReporteMensual(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletResponse response) throws IOException {
        adminReportService.exportarReporteMensual(year, month, response);
    }

    // LISTA DE TICKETS
    @GetMapping("/tickets")
    public String verTodosLosTickets(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     Model model) {
        page = Math.max(page, 0);
        size = normalizarSize(size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("marcaTemporal").descending());
        Page<Ticket> pagina = ticketService.obtenerTodosOrdenados(pageable);

        model.addAttribute("tickets", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("size", size);

        return "admin/admin";
    }


    // FILTROS
    @GetMapping("/tickets/buscar")
    public String filtrarTickets(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        page = Math.max(page, 0);
        size = normalizarSize(size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("marcaTemporal").descending());
        Page<Ticket> pagina = ticketService.filtrarTickets(estado, prioridad, busqueda, pageable);

        model.addAttribute("tickets", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("estado", estado);
        model.addAttribute("prioridad", prioridad);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("size", size);
        return "admin/admin";
    }

    // DETALLE TICKET
    @GetMapping("/ticket/{id}")
    public String verDetalleTicket(@PathVariable Integer id, Model model) {

        Ticket ticket = ticketService.obtenerPorId(id);

        model.addAttribute("ticket", ticket);
        model.addAttribute("medias", mediaService.obtenerMediasPorTicket(id));
        model.addAttribute("comentarios", ticketService.obtenerComentariosPorTicket(id));

        return "admin/detalleTicket";
    }

    //ENVIAR ARCHIVOS
    @PostMapping("/tickets/responder")
    public String responderTicketAdmin(
            @RequestParam Integer ticketId,
            @RequestParam String mensaje,
            @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        try {
            Ticket ticket = ticketService.obtenerPorId(ticketId);

            // Obtener nombre real del admin logueado
            String nombreAdmin = obtenerNombreAdmin(authentication);

            ticketService.agregarRespuestaAdmin(ticketId, mensaje, archivos, nombreAdmin);

            emailService.enviarRespuestaTicket(
                    ticket.getEmailActual(),
                    ticket.getNombreActual(),
                    mensaje,
                    ticketId
            );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Respuesta enviada y correo notificado"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "Error al enviar respuesta"
            );

        }

        return "redirect:/admin/ticket/" + ticketId;
    }

    // ACTUALIZAR TICKET
    @PostMapping("/ticket/actualizar")
    public String actualizarTicket(
            @RequestParam Integer id,
            @RequestParam String estado,
            @RequestParam String priority,
            @RequestParam(required = false) String encargado,
            @RequestParam(required = false) String respuesta,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        Ticket ticket = ticketService.obtenerPorId(id);

        // Obtener nombre real del admin logueado
        String nombreAdmin = obtenerNombreAdmin(authentication);

        // Guardar estado anterior
        boolean estabaCerrado = "CERRADO".equalsIgnoreCase(ticket.getEstado());
        String estadoAnterior = ticket.getEstado();

        ticket.setEstado(estado);
        ticket.setPriority(priority);
        ticket.setAtendidoPor(encargado);

        if (respuesta != null && !respuesta.isBlank()) {
            try {
                ticketService.agregarRespuestaAdmin(id, respuesta, null, nombreAdmin);
            } catch (Exception e) {
                throw new RuntimeException("Error al responder ticket", e);
            }
        }

        // Fecha de cierre
        if ("CERRADO".equalsIgnoreCase(estado)) {
            ticket.setFechaCierre(java.time.LocalDateTime.now());
        } else {
            ticket.setFechaCierre(null);
        }

        ticketService.guardar(ticket);

        // Notificación al dueño del ticket si cambió el estado
        if (!estado.equalsIgnoreCase(estadoAnterior) && ticket.getUsuario() != null) {
            notificacionService.crearParaUsuario(
                    "Tu ticket #" + id + " cambio de estado: " + estadoAnterior + " → " + estado,
                    "ESTADO_ACTUALIZADO",
                    ticket,
                    ticket.getUsuario()
            );
        }

        // Enviar correo si se cerró ahora
        if ("CERRADO".equalsIgnoreCase(estado) && !estabaCerrado) {
            emailService.enviarTicketCerrado(
                    ticket.getEmailActual(),
                    ticket.getNombreActual(),
                    ticket.getId()
            );
        }

        // Enviar correo si hubo respuesta
        if (respuesta != null && !respuesta.isBlank()) {
            emailService.enviarRespuestaTicket(
                    ticket.getEmailActual(),
                    ticket.getNombreActual(),
                    respuesta,
                    ticket.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Ticket actualizado y respuesta enviada"
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Ticket actualizado correctamente"
            );
        }

        return "redirect:/admin/ticket/" + id;
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        return "admin/formUsuario";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Integer id, Model model) {
        Usuario usuario = adminUsuarioService.obtenerPorId(id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        return "admin/formUsuario";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        adminUsuarioService.guardarUsuario(usuario);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario registrado correctamente");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        adminUsuarioService.eliminarUsuario(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente");
        return "redirect:/admin/usuarios";
    }

    /** Obtiene el nombre del admin logueado, o "ADMIN" si falla */
    private String obtenerNombreAdmin(Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario admin = usuarioService.obtenerPorEmail(email);
            return admin.getNombre();
        } catch (Exception e) {
            return "ADMIN";
        }

    }

    private int normalizarSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

}
