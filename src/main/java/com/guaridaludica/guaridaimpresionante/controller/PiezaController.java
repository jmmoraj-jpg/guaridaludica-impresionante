package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.FavoritoRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
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

    public PiezaController(ProductoRepository productoRepository, FavoritoRepository favoritoRepository) {
        this.productoRepository = productoRepository;
        this.favoritoRepository = favoritoRepository;
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
        return "pieza";
    }
}