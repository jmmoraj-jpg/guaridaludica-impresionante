package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.ImagenProducto;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.service.ProductoAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {

    private static final List<String> CATEGORIAS = List.of("Figuras", "Juguetes", "Decoración", "Regalos");

    private final ProductoRepository productoRepository;
    private final ProductoAdminService servicioProductoAdminService;

    public AdminProductoController(ProductoRepository productoRepository, ProductoAdminService servicioProductoAdminService) {
        this.productoRepository = productoRepository;
        this.servicioProductoAdminService = servicioProductoAdminService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoRepository.findAllByOrderByIdDesc());
        return "admin/productos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        prepararFormulario(model, new ProductoForm(), List.of());
        return "admin/producto-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Producto p = productoRepository.findWithImagenesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        prepararFormulario(model, ProductoForm.desde(p), p.getImagenes());
        return "admin/producto-form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("form") ProductoForm form,
                          BindingResult result,
                          @RequestParam(value = "nuevas", required = false) List<MultipartFile> nuevas,
                          @RequestParam(value = "eliminar", required = false) List<Long> eliminar,
                          @RequestParam(value = "portada", required = false) String portada,
                          Model model,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            List<ImagenProducto> existentes = form.getId() == null
                    ? List.of()
                    : productoRepository.findWithImagenesById(form.getId())
                        .map(Producto::getImagenes).orElse(List.of());
            model.addAttribute("categorias", CATEGORIAS);
            model.addAttribute("imagenes", existentes);
            return "admin/producto-form";
        }
        servicioProductoAdminService.guardar(form, nuevas, eliminar, portada);
        redirect.addFlashAttribute("mensaje", "Pieza guardada");
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/visibilidad")
    public String visibilidad(@PathVariable Long id) {
        servicioProductoAdminService.cambiarVisibilidad(id);
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id, RedirectAttributes redirect) {
        servicioProductoAdminService.borrar(id);
        redirect.addFlashAttribute("mensaje", "Pieza borrada");
        return "redirect:/admin/productos";
    }

    private void prepararFormulario(Model model, ProductoForm form, List<ImagenProducto> imagenes) {
        model.addAttribute("form", form);
        model.addAttribute("imagenes", imagenes);
        model.addAttribute("categorias", CATEGORIAS);
    }
}