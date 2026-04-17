package com.maple.back.services.auth;

import com.maple.back.dto.RegisterRequest;
import com.maple.back.model.Rol;
import com.maple.back.model.Usuario;
import com.maple.back.model.UsuarioPendiente;
import com.maple.back.repository.UsuarioRepository;
import com.maple.back.repository.UsuarioPendienteRepository;
import com.maple.back.services.email.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegisterService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPendienteRepository pendienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final EmailService emailService;

    public RegisterService(UsuarioRepository usuarioRepository,
                           UsuarioPendienteRepository pendienteRepository,
                           PasswordEncoder passwordEncoder,
                           ValidationService validationService,
                           EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.pendienteRepository = pendienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.emailService = emailService;
    }

    /**
     * Registra un nuevo usuario usando DTO
     */
    public Optional<String> registerUser(RegisterRequest request) {
        return registerUser(
            request.getNombre(),
            request.getEmail(),
            request.getPassword(),
            request.getConfirmPassword(),
            request.getCedula(),
            request.getTelefono(),
            request.getCiudad(),
            request.getPunto(),
            request.getCargo()
        );
    }

    /**
     * Registra un nuevo usuario como PENDIENTE y envía email de verificación
     */
    public Optional<String> registerUser(
            String nombre,
            String email,
            String password,
            String confirmPassword,
            String cedula,
            String telefono,
            String ciudad,
            String punto,
            String cargo
    ) {
        // Normalize inputs
        nombre = nombre == null ? null : nombre.trim();
        email = email == null ? null : email.trim().toLowerCase();
        cedula = cedula == null ? null : cedula.trim();
        telefono = telefono == null ? null : telefono.trim();
        ciudad = ciudad == null ? null : ciudad.trim().toUpperCase();
        punto = punto == null ? null : punto.trim().toUpperCase();
        cargo = cargo == null ? null : cargo.trim().toUpperCase();

        // Validar campos
        List<String> errores = validationService.validarRegistro(
            nombre, email, password, confirmPassword, cedula, telefono, ciudad, punto, cargo
        );

        if (!errores.isEmpty()) {
            return Optional.of(errores.get(0));
        }

        // Verificar si el email ya existe como usuario activo
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return Optional.of("El email ya esta registrado");
        }

        // Si ya hay un pendiente con ese email, borrarlo (reenvío)
        pendienteRepository.findByEmail(email).ifPresent(pendienteRepository::delete);

        // Crear usuario pendiente con token
        String token = UUID.randomUUID().toString();

        UsuarioPendiente pendiente = new UsuarioPendiente();
        pendiente.setNombre(nombre);
        pendiente.setEmail(email);
        pendiente.setContrasena(passwordEncoder.encode(password));
        pendiente.setCedula(cedula);
        pendiente.setTelefono(telefono);
        pendiente.setCiudad(ciudad);
        pendiente.setPunto(punto);
        pendiente.setCargo(cargo);
        pendiente.setToken(token);
        pendiente.setExpiracion(LocalDateTime.now().plusHours(24));

        pendienteRepository.save(pendiente);

        // Enviar email de verificación
        emailService.enviarVerificacionCuenta(email, nombre, token);

        return Optional.empty();
    }

    /**
     * Verifica el token y activa la cuenta
     */
    public Optional<String> verificarCuenta(String token) {
        Optional<UsuarioPendiente> opt = pendienteRepository.findByToken(token);

        if (opt.isEmpty()) {
            return Optional.of("Token invalido o ya fue utilizado.");
        }

        UsuarioPendiente pendiente = opt.get();

        // Verificar expiración
        if (pendiente.getExpiracion().isBefore(LocalDateTime.now())) {
            pendienteRepository.delete(pendiente);
            return Optional.of("El enlace de verificacion ha expirado. Registrate nuevamente.");
        }

        // Verificar que no se haya registrado mientras tanto
        if (usuarioRepository.findByEmail(pendiente.getEmail()).isPresent()) {
            pendienteRepository.delete(pendiente);
            return Optional.of("Esta cuenta ya fue activada.");
        }

        // Crear usuario real
        Usuario usuario = new Usuario();
        usuario.setNombre(pendiente.getNombre());
        usuario.setEmail(pendiente.getEmail());
        usuario.setContrasena(pendiente.getContrasena());
        usuario.setCedula(pendiente.getCedula());
        usuario.setTelefono(pendiente.getTelefono());
        usuario.setCiudad(pendiente.getCiudad());
        usuario.setPunto(pendiente.getPunto());
        usuario.setCargo(pendiente.getCargo());
        usuario.setRol(Rol.USER);

        usuarioRepository.save(usuario);

        // Borrar pendiente
        pendienteRepository.delete(pendiente);

        return Optional.empty();
    }

    /**
     * Verifica si un email ya está registrado
     */
    public boolean emailYaRegistrado(String email) {
        return usuarioRepository.findByEmail(email).isPresent()
                || pendienteRepository.findByEmail(email).isPresent();
    }
}
