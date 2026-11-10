package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.repository.CategoriaRepository;
import com.guaridaludica.guaridaimpresionante.service.CategoriaService;
import com.guaridaludica.guaridaimpresionante.util.Paginas;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private static final int POR_PAGINA = 15;

    private final CategoriaRepository categoriaRepository;
    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaRepository categoriaRepository,
            CategoriaService categoriaService) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String q,
            Model model) {
        model.addAttribute("form", new CategoriaForm());
        cargarListado(model, pagina, q);
        return "admin/categorias";
    }

    private void cargarListado(Model model, int pagina, String q) {
        String busqueda = (q == null || q.isBlank()) ? null : q.trim();

        Page<Categoria> page = Paginas.pedir(pagina, i -> {
            Pageable pageable = PageRequest.of(i, POR_PAGINA,
                    Sort.by("orden").ascending().and(Sort.by("nombre").ascending()));
            return busqueda == null
                    ? categoriaRepository.findAll(pageable)
                    : categoriaRepository.findByNombreContainingIgnoreCase(busqueda, pageable);
        });

        model.addAttribute("categorias", page.getContent());
        Paginas.atributos(model, page);
        model.addAttribute("q", busqueda);
        model.addAttribute("extraPaginacion", Paginas.extraBusqueda(busqueda));
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
                cargarListado(model, 1, null);
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