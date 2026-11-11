package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Usuario;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class CuentaController {

    private final UsuarioRepository usuarioRepository;
    private final CuentaService cuentaService;

    public CuentaController(UsuarioRepository usuarioRepository, CuentaService cuentaService) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaService = cuentaService;
    }

    @GetMapping("/cuenta")
    public String cuenta(Authentication authentication, Model model) {
        Optional<Usuario> usuario = usuarioRepository.findByEmailIgnoreCase(authentication.getName());
        if (usuario.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("usuario", usuario.get());
        return "cuenta";
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
            return "redirect:/cuenta";
        }

        try {
            cuentaService.borrarCuenta(authentication.getName(), password);
        } catch (BadCredentialsException e) {
            redirect.addFlashAttribute("error", "La contraseña no es correcta.");
            return "redirect:/cuenta";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta";
        }

        // Cierra la sesión de la cuenta que acaba de desaparecer
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?cuentaBorrada";
    }
}