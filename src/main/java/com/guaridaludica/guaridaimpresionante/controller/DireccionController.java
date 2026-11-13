package com.guaridaludica.guaridaimpresionante.controller;


import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.guaridaludica.guaridaimpresionante.model.Direccion;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.DireccionRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/perfil/direcciones")
public class DireccionController {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository; // Para buscar al usuario de forma segura con el email

    public DireccionController(DireccionRepository direccionRepository, UsuarioRepository usuarioRepository) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listarDirecciones(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        
        List<Direccion> direcciones = direccionRepository.findByUsuario(usuario);
        
        model.addAttribute("direcciones", direcciones);
        model.addAttribute("nuevaDireccion", new Direccion());
        return "perfil/direcciones";
    }

    @PostMapping("/guardar")
    public String guardarDireccion(@Valid @ModelAttribute("nuevaDireccion") Direccion direccion,
                                   BindingResult result,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Revisa los campos de la dirección.");
            return "redirect:/perfil";
        }

        List<Direccion> existentes = direccionRepository.findByUsuario(usuario);
        if (direccion.isPrincipal() || existentes.isEmpty()) {
            existentes.forEach(d -> {
                d.setPrincipal(false);
                direccionRepository.save(d);
            });
            direccion.setPrincipal(true);
        }

        direccion.setUsuario(usuario);
        direccionRepository.save(direccion);
        
        redirectAttributes.addFlashAttribute("exito", "Dirección guardada correctamente.");
        return "redirect:/perfil";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarDireccion(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        
        direccionRepository.findByIdAndUsuario(id, usuario).ifPresent(direccionRepository::delete);
        
        redirectAttributes.addFlashAttribute("exito", "Dirección eliminada.");
        return "redirect:/perfil";
    }
}