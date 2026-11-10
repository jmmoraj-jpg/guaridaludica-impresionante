package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.repository.CategoriaRepository;
import com.guaridaludica.guaridaimpresionante.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class AdminCategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaRepository categoriaRepository,
            CategoriaService categoriaService) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByOrdenAscNombreAsc());
        model.addAttribute("form", new CategoriaForm());
        return "admin/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("form", CategoriaForm.desde(c));
        return "admin/categoria-form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("form") CategoriaForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (!result.hasFieldErrors("nombre")
                && categoriaService.nombreEnUso(form.getNombre(), form.getId())) {
            result.rejectValue("nombre", "duplicado", "Ya existe una categoría con ese nombre");
        }
        if (result.hasErrors()) {
            if (form.getId() == null) {
                model.addAttribute("categorias", categoriaRepository.findAllByOrderByOrdenAscNombreAsc());
                return "admin/categorias";
            }
            return "admin/categoria-form";
        }
        categoriaService.guardar(form);
        redirect.addFlashAttribute("mensaje", "Categoría guardada");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/visibilidad")
    public String visibilidad(@PathVariable Long id) {
        categoriaService.cambiarVisibilidad(id);
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id, RedirectAttributes redirect) {
        categoriaService.borrar(id);
        redirect.addFlashAttribute("mensaje", "Categoría borrada");
        return "redirect:/admin/categorias";
    }
}