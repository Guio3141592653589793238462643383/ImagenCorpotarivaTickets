package com.austral.back.controller;

import com.austral.back.dto.RegisterRequest;
import com.austral.back.services.auth.RegisterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String telefono,
            @RequestParam String punto,
            @RequestParam String cargo,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        RegisterRequest request = new RegisterRequest(
            nombre, email, password, confirmPassword, telefono, punto, cargo
        );

        Optional<String> error = registerService.registerUser(request);

        if (error.isPresent()) {
            model.addAttribute("error", error.get());
            model.addAttribute("registerRequest", request);
            return "register";
        }

        redirectAttributes.addFlashAttribute("success",
            "Te enviamos un correo de verificacion. Revisa tu bandeja de entrada para activar tu cuenta.");
        return "redirect:/";
    }

    /** Endpoint donde llega el usuario al hacer click en el link del correo */
    @GetMapping("/verificar")
    public String verificarCuenta(@RequestParam String token, RedirectAttributes redirectAttributes) {

        Optional<String> error = registerService.verificarCuenta(token);

        if (error.isPresent()) {
            redirectAttributes.addFlashAttribute("error", error.get());
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("success",
            "Tu cuenta ha sido verificada exitosamente. Ya puedes iniciar sesion.");
        return "redirect:/";
    }

    @GetMapping("/register/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return !registerService.emailYaRegistrado(email);
    }
}
