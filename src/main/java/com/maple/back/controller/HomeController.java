package com.maple.back.controller;

import com.maple.back.model.Ticket;
import com.maple.back.model.TicketMedia;
import com.maple.back.model.Usuario;
import com.maple.back.services.auth.AuthenticationService;
import com.maple.back.services.media.MediaService;
import com.maple.back.services.password.PasswordResetService;
import com.maple.back.services.ticket.TicketService;
import com.maple.back.services.user.UsuarioService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
@Controller
public class HomeController {

    private final UsuarioService usuarioService;
    private final TicketService ticketService;
    private final AuthenticationService authenticationService;
    private final MediaService mediaService;
    private final PasswordResetService passwordResetService;

    public HomeController(UsuarioService usuarioService,
                          TicketService ticketService,
                          AuthenticationService authenticationService,
                          MediaService mediaService,
                          PasswordResetService passwordResetService) {

        this.usuarioService = usuarioService;
        this.ticketService = ticketService;
        this.authenticationService = authenticationService;
        this.mediaService = mediaService;
        this.passwordResetService = passwordResetService;
    }

    // 🔹 Página principal (login)
    @GetMapping("/")
    public String rootRedirect(Authentication authentication) {
        return authenticationService.obtenerRedireccionSegunRol(authentication);
    }

    // 🔹 Procesar recuperación
    @PostMapping("/recover")
    public String procesarRecuperacion(@RequestParam String email,
                                       Model model) {

        boolean enviado = passwordResetService.generarToken(email);

        if (!enviado) {
            model.addAttribute("error",
                    "No existe una cuenta registrada con ese correo.");
            return "recuperar";
        }

        model.addAttribute("message",
                "Se ha enviado un enlace de recuperación.");
        return "recuperar";
    }
    @GetMapping("/recover")
    public String mostrarFormularioRecuperacion() {
        return "recuperar";
    }
    // 🔹 Mostrar formulario reset
    @GetMapping("/reset-password")
    public String mostrarFormularioReset(@RequestParam("token") String token,
                                         Model model) {

        boolean valido = passwordResetService.validarToken(token);

        if (!valido) {
            model.addAttribute("error", "Token inválido o expirado.");
            return "recuperar";
        }

        model.addAttribute("token", token);
        return "reset_password";
    }

    // 🔹 Procesar reset
    @PostMapping("/reset-password")
    public String procesarReset(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            model.addAttribute("token", token); // mantener token en el formulario
            return "reset_password";
        }

        boolean actualizado = passwordResetService.resetearPassword(token, password);

        if (!actualizado) {
            model.addAttribute("error", "Token inválido o expirado.");
            return "recuperar";
        }

        model.addAttribute("message", "Tu contraseña fue actualizada correctamente.");
        return "redirect:/";
    }


    // 🔹 Ver detalle de ticket
    @GetMapping("/ticket/{ticketId}")
    public String verDetalleTicket(@PathVariable Integer ticketId,
                                   Model model,
                                   Authentication authentication) {

        Ticket ticket = ticketService.obtenerPorId(ticketId);

        // Verificar que el usuario logueado es el dueño del ticket
        String emailLogueado = authenticationService.obtenerEmailUsuario(authentication);
        if (ticket.getUsuario() == null || !ticket.getUsuario().getEmail().equals(emailLogueado)) {
            // Si es admin, dejarlo pasar; si no, fuera
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!esAdmin) {
                return "redirect:/menu";
            }
        }

        List<TicketMedia> medias = mediaService.obtenerMediasPorTicket(ticketId);
        var comentarios = ticketService.obtenerComentariosPorTicket(ticketId);

        model.addAttribute("ticket", ticket);
        model.addAttribute("medias", medias);
        model.addAttribute("comentarios", comentarios);

        return "tickets";
    }

    // 🔹 Menú usuario
    @GetMapping("/menu")
    public String home(Model model,
                       Authentication authentication,
                       @RequestParam(defaultValue = "0") int page) {

        if (!authenticationService.estaAutenticado(authentication)) {
            return "redirect:/";
        }

        String email = authenticationService.obtenerEmailUsuario(authentication);
        Usuario usuario = usuarioService.obtenerPorEmail(email);

        Pageable pageable = PageRequest.of(page, 5);
        Page<Ticket> pagina = ticketService.obtenerTicketsUsuario(email, pageable);

        List<Ticket> listaTickets = pagina.getContent();

        model.addAttribute("usuario", usuario);
        model.addAttribute("tickets", listaTickets);
        model.addAttribute("pagina", pagina);

        return "menu";
    }

    // 🔹 Perfil usuario
    @GetMapping("/perfil")
    public String miPerfil(Model model, Authentication authentication) {

        if (!authenticationService.estaAutenticado(authentication)) {
            return "redirect:/";
        }

        String email = authenticationService.obtenerEmailUsuario(authentication);
        Usuario usuario = usuarioService.obtenerPorEmail(email);

        model.addAttribute("usuario", usuario);

        return "mi_perfil";
    }


}