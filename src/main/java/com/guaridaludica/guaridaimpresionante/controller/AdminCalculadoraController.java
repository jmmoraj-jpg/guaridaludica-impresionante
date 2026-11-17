package com.guaridaludica.guaridaimpresionante.controller;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String mostrar(Model model) {
        model.addAttribute("productos", productoRepository.findAll(Sort.by("nombre").ascending()));
        model.addAttribute("filamentos", filamentoRepository.findAll(Sort.by("nombre").ascending()));
        return "admin/calculadora";
    }
}