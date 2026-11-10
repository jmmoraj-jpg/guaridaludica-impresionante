package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.repository.CategoriaRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.service.FavoritoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class CatalogoController {

    private static final int MAX_FILTROS = 6;   // categorías que salen como botones; el resto, en "Más categorías"

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FavoritoService favoritoService;

    @Value("${app.banner.titulo}")
    private String bannerTitulo;

    @Value("${app.banner.mensaje}")
    private String bannerMensaje;

    public CatalogoController(ProductoRepository productoRepository,
                              CategoriaRepository categoriaRepository,
                              FavoritoService favoritoService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.favoritoService = favoritoService;
    }

    @GetMapping("/")
    public String inicio(@RequestParam(required = false) String categoria,
                         Principal principal, Model model) {
        String slug = (categoria == null || categoria.isBlank()) ? null : categoria.trim();

        // Solo categorías visibles y con alguna pieza visible
        List<Categoria> todas = categoriaRepository.visiblesConPiezas();
        Categoria actual = slug == null ? null
                : todas.stream().filter(c -> c.getSlug().equals(slug)).findFirst().orElse(null);

        List<Categoria> principales = new ArrayList<>(todas.subList(0, Math.min(MAX_FILTROS, todas.size())));
        List<Categoria> extra = new ArrayList<>(todas.subList(principales.size(), todas.size()));
        // Si la categoría activa estaba en "Más categorías", se muestra como botón para verla seleccionada
        if (actual != null && extra.remove(actual)) {
            principales.add(actual);
        }

        model.addAttribute("bannerTitulo", bannerTitulo);
        model.addAttribute("bannerMensaje", bannerMensaje);
        model.addAttribute("categoriasPrincipales", principales);
        model.addAttribute("categoriasExtra", extra);
        model.addAttribute("categoriaActual", actual);
        model.addAttribute("productos", actual == null
                ? productoRepository.findByVisibleTrue()
                : productoRepository.buscarPorCategoria(actual.getSlug()));
        model.addAttribute("favoritosIds", principal == null
                ? Set.<Long>of()
                : favoritoService.idsFavoritos(principal.getName()));
        return "index";
    }
}