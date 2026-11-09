package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Rol;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registro", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registro") RegistroForm form,
                                   BindingResult result) {
        if (!Objects.equals(form.getPassword(), form.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "no.coincide", "Las contraseñas no coinciden");
        }
        if (!result.hasFieldErrors("email")
                && usuarioRepository.existsByEmailIgnoreCase(form.getEmail().trim())) {
            result.rejectValue("email", "duplicado", "Ya existe una cuenta con ese email");
        }
        if (result.hasErrors()) {
            return "registro";
        }

        Usuario u = new Usuario();
        u.setNombre(form.getNombre().trim());
        u.setEmail(form.getEmail().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.setRol(Rol.USUARIO);
        usuarioRepository.save(u);

        return "redirect:/login?registrado";
    }
}