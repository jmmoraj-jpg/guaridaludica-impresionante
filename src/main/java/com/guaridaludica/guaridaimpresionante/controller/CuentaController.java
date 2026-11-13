package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Direccion;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.DireccionRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import com.guaridaludica.guaridaimpresionante.service.CuentaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class CuentaController {

    private final UsuarioRepository usuarioRepository;
    private final DireccionRepository direccionRepository;
    private final CuentaService cuentaService;

    public CuentaController(UsuarioRepository usuarioRepository, DireccionRepository direccionRepository, CuentaService cuentaService) {
        this.usuarioRepository = usuarioRepository;
        this.direccionRepository = direccionRepository;
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public String cuenta(Authentication authentication, Model model) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        
        // Recuperamos también las direcciones asociadas para listarlas en la misma página
        List<Direccion> direcciones = direccionRepository.findByUsuario(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("direcciones", direcciones);
        
        // Si el modelo no trae ya una nuevaDireccion (por ejemplo, tras un fallo de validación), se la pasamos para el modal
        if (!model.containsAttribute("nuevaDireccion")) {
            model.addAttribute("nuevaDireccion", new Direccion());
        }

        return "perfil/cuenta";
    }

    @PostMapping("/cuenta/borrar")
    public String borrar(@RequestParam String password,
                         @RequestParam(defaultValue = "false") boolean confirmar,
                         Authentication authentication,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirect) {

        if (!confirmar) {
            redirect.addFlashAttribute("error", "Marca la casilla para confirmar que quieres borrar tu cuenta.");
            return "redirect:/perfil";
        }

        try {
            cuentaService.borrarCuenta(authentication.getName(), password);
        } catch (BadCredentialsException e) {
            redirect.addFlashAttribute("error", "La contraseña no es correcta.");
            return "redirect:/perfil";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/perfil";
        }

        // Cierra la sesión de la cuenta que acaba de desaparecer
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?cuentaBorrada";
    }
}