package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.FavoritoRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.service.PiezaService;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PiezaController {

    private final ProductoRepository productoRepository;
    private final FavoritoRepository favoritoRepository;
    private final PiezaService piezaService;

    public PiezaController(ProductoRepository productoRepository, FavoritoRepository favoritoRepository, PiezaService piezaService) {
        this.productoRepository = productoRepository;
        this.favoritoRepository = favoritoRepository;
        this.piezaService = piezaService;
    }

    @GetMapping("/pieza/{id}")
    public String detalle(@PathVariable Long id, Principal principal, Model model) {
        Producto pieza = productoRepository.findWithImagenesById(id)
                .filter(Producto::isVisible)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean esFavorito = principal != null
                && favoritoRepository.existsByUsuarioEmailIgnoreCaseAndProductoId(principal.getName(), id);
        model.addAttribute("pieza", pieza);
        model.addAttribute("esFavorito", esFavorito);
        model.addAttribute("relacionadas", piezaService.buscarRelacionadas(pieza));
        return "pieza";
    }
}