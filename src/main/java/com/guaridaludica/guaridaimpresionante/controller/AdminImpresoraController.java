package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Impresion;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.service.ImpresionService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/impresora")
public class AdminImpresoraController {

    private final ImpresionService servicio;
    private final ProductoRepository productoRepository;

    public AdminImpresoraController(ImpresionService servicio, ProductoRepository productoRepository) {
        this.servicio = servicio;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public String ver(Model model) {
        List<Impresion> enCurso = servicio.enCurso();

        // Ids de las piezas que ya están en la impresora
        List<Long> idsEnCurso = new ArrayList<>();
        for (Impresion i : enCurso) {
            idsEnCurso.add(i.getProducto().getId());
        }

        // Solo se ofrecen las que aún no están
        List<Producto> disponibles = new ArrayList<>();
        for (Producto p : productoRepository.findAll(Sort.by("nombre"))) {
            if (!idsEnCurso.contains(p.getId())) {
                disponibles.add(p);
            }
        }

        model.addAttribute("impresiones", enCurso);
        model.addAttribute("productos", disponibles);
        model.addAttribute("max", ImpresionService.MAX);
        return "admin/impresora";
    }

    @PostMapping("/anadir")
    public String anadir(@RequestParam Long productoId,
            @RequestParam(defaultValue = "0") int progreso,
            RedirectAttributes redirect) {
        String error = servicio.anadir(productoId, progreso);
        if (error != null) {
            redirect.addFlashAttribute("error", error);
        } else {
            redirect.addFlashAttribute("mensaje", "Pieza añadida a la impresora");
        }
        return "redirect:/admin/impresora";
    }

    @PostMapping("/{id}/progreso")
    public String progreso(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int progreso,
            RedirectAttributes redirect) {
        servicio.actualizar(id, progreso);
        redirect.addFlashAttribute("mensaje", "Avance actualizado");
        return "redirect:/admin/impresora";
    }

    @PostMapping("/{id}/quitar")
    public String quitar(@PathVariable Long id, RedirectAttributes redirect) {
        servicio.quitar(id);
        redirect.addFlashAttribute("mensaje", "Pieza quitada de la impresora");
        return "redirect:/admin/impresora";
    }
}