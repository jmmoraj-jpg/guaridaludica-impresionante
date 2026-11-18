package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.service.AdminUsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminUsuarioController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", adminUsuarioService.obtenerTodosLosUsuarios());
        return "admin/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminUsuarioService.eliminarUsuarioConDependencias(id);
            redirectAttributes.addFlashAttribute("exito", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el usuario debido a datos asociados.");
        }
        return "redirect:/admin/usuarios";
    }
}