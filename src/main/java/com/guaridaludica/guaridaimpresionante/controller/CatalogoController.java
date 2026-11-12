package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.model.EstadoConversacion;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.CategoriaRepository;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.service.FavoritoService;
import com.guaridaludica.guaridaimpresionante.service.ImpresionService;
import com.guaridaludica.guaridaimpresionante.util.Paginas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final int MAX_FILTROS = 6; // categorías que salen como botones; el resto, en "Más categorías"
    private static final int PRODUCTOS_POR_PAGINA = 12;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ConversacionRepository conversacionRepository;
    private final FavoritoService favoritoService;
    private final ImpresionService impresionService;

    @Value("${app.banner.titulo}")
    private String bannerTitulo;

    @Value("${app.banner.mensaje}")
    private String bannerMensaje;

    public CatalogoController(ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            ConversacionRepository conversacionRepository,
            FavoritoService favoritoService,
            ImpresionService impresionService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.conversacionRepository = conversacionRepository;
        this.favoritoService = favoritoService;
        this.impresionService = impresionService;
    }

    @GetMapping("/")
    public String inicio(@RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "1") int pagina,
            Principal principal, Model model) {
        String slug = (categoria == null || categoria.isBlank()) ? null : categoria.trim();

        List<Categoria> todas = categoriaRepository.visiblesConPiezas();
        Categoria actual = slug == null ? null
                : todas.stream().filter(c -> c.getSlug().equals(slug)).findFirst().orElse(null);

        List<Categoria> principales = new ArrayList<>(todas.subList(0, Math.min(MAX_FILTROS, todas.size())));
        List<Categoria> extra = new ArrayList<>(todas.subList(principales.size(), todas.size()));
        if (actual != null && extra.remove(actual)) {
            principales.add(actual);
        }

        Page<Producto> page = Paginas.pedir(pagina, i -> {
            Pageable pageable = PageRequest.of(i, PRODUCTOS_POR_PAGINA, Sort.by(Sort.Direction.DESC, "id"));
            return actual == null
                    ? productoRepository.findByVisibleTrue(pageable)
                    : productoRepository.buscarPorCategoria(actual.getSlug(), pageable);
        });

        model.addAttribute("bannerTitulo", bannerTitulo);
        model.addAttribute("bannerMensaje", bannerMensaje);
        model.addAttribute("categoriasPrincipales", principales);
        model.addAttribute("categoriasExtra", extra);
        model.addAttribute("categoriaActual", actual);
        model.addAttribute("productos", page.getContent());
        Paginas.atributos(model, page);
        model.addAttribute("extraPaginacion", actual == null ? "" : "&categoria=" + actual.getSlug());
        model.addAttribute("favoritosIds", principal == null
                ? Set.<Long>of()
                : favoritoService.idsFavoritos(principal.getName()));

        cargarImpresora(model);
        model.addAttribute("novedad",
                productoRepository.findFirstByNovedadTrueAndVisibleTrueOrderByIdDesc().orElse(null));

        return "index";
    }

    // Solo la tarjeta de la impresora, para que impresora-vivo.js la actualice sin recargar la portada
    @GetMapping("/impresora/tarjeta")
    public String tarjetaImpresora(Model model) {
        cargarImpresora(model);
        return "fragments/impresora :: tarjeta";
    }

    // Impresiones en curso y solicitudes en cola: los mismos datos para la portada y para la tarjeta
    private void cargarImpresora(Model model) {
        List<EstadoConversacion> enEspera = List.of(EstadoConversacion.SOLICITADA, EstadoConversacion.ACEPTADA);
        model.addAttribute("impresiones", impresionService.enCurso());
        model.addAttribute("cola", conversacionRepository.countByEstadoIn(enEspera));
    }
}