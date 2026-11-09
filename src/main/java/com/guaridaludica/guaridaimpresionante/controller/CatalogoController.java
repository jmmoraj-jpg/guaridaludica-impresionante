package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.service.FavoritoService;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CatalogoController {

    private final ProductoRepository productoRepository;
    private final FavoritoService favoritoService;

    @Value("${app.banner.titulo}")
    private String bannerTitulo;

    @Value("${app.banner.mensaje}")
    private String bannerMensaje;

    @Value("${app.banner.advertencia}")
    private String bannerAdvertencia;

    public CatalogoController(ProductoRepository productoRepository, FavoritoService favoritoService) {
        this.productoRepository = productoRepository;
        this.favoritoService = favoritoService;
    }

    private static final List<String> CATEGORIAS = List.of("Figuras", "Juguetes", "Decoración", "Regalos");

    @GetMapping("/")
    public String inicio(@RequestParam(required = false) String categoria,
            Principal principal, Model model) {
        String cat = (categoria == null || categoria.isBlank()) ? null : categoria;

        model.addAttribute("bannerTitulo", bannerTitulo);
        model.addAttribute("bannerMensaje", bannerMensaje);
        model.addAttribute("bannerAdvertencia", bannerAdvertencia);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("categoriaActual", cat);
        model.addAttribute("productos", cat == null
                ? productoRepository.findByVisibleTrue()
                : productoRepository.findByVisibleTrueAndCategoria(cat));
        model.addAttribute("favoritosIds", principal == null
                ? Set.<Long>of()
                : favoritoService.idsFavoritos(principal.getName()));
        return "index";
    }
}