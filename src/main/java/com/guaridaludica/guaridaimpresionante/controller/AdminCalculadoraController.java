package com.guaridaludica.guaridaimpresionante.controller;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.FilamentoRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;

@Controller
@RequestMapping("/admin/calculadora")
public class AdminCalculadoraController {

    private final ProductoRepository productoRepository;
    private final FilamentoRepository filamentoRepository;

    public AdminCalculadoraController(ProductoRepository productoRepository,
            FilamentoRepository filamentoRepository) {
        this.productoRepository = productoRepository;
        this.filamentoRepository = filamentoRepository;
    }

    @GetMapping
    public String mostrar(@RequestParam(required = false) Long productoId, Model model) {
        model.addAttribute("productos", productoRepository.findAll(Sort.by("nombre").ascending()));
        model.addAttribute("filamentos", filamentoRepository.findAll(Sort.by("nombre").ascending()));
        model.addAttribute("productoIdSeleccionado", productoId);
        return "admin/calculadora";
    }

    @PostMapping("/guardar-calculo")
    public String guardarCalculo(@RequestParam("productoId") Long productoId,
            @RequestParam("costeProduccion") Double costeProduccion,
            @RequestParam("margenBeneficio") Double margenBeneficio,
            @RequestParam("precioSugerido") Double precioSugerido,
            RedirectAttributes redirectAttributes) {

        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            producto.setCosteProduccion(costeProduccion);
            producto.setMargenBeneficioAplicado(margenBeneficio);
            producto.setPrecioSugerido(precioSugerido);

            productoRepository.save(producto);
            redirectAttributes.addFlashAttribute("exito",
                    "¡Cálculo guardado correctamente en la pieza '" + producto.getNombre() + "'!");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró la pieza seleccionada.");
        }

        return "redirect:/admin/calculadora";
    }
}