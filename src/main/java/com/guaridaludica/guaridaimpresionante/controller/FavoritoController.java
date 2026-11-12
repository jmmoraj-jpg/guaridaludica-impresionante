package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.service.FavoritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping("/favoritos")
    public String lista(Principal principal, Model model) {
        model.addAttribute("productos", favoritoService.productosFavoritos(principal.getName()));
        return "favoritos";
    }

    // Lo usa favoritos.js: cambia el favorito sin recargar la página
    @PostMapping(value = "/favoritos/{productoId}", headers = "X-Requested-With=fetch")
    public ResponseEntity<Void> alternarSinRecargar(@PathVariable Long productoId, Principal principal) {
        favoritoService.alternar(principal.getName(), productoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/favoritos/{productoId}")
    public String alternar(@PathVariable Long productoId,
            @RequestParam(required = false) String volver,
            Principal principal) {
        favoritoService.alternar(principal.getName(), productoId);

        // Solo se aceptan rutas internas, para evitar redirecciones a otras webs
        boolean rutaInterna = volver != null && volver.startsWith("/") && !volver.startsWith("//");
        if (!rutaInterna) {
            return "redirect:/pieza/" + productoId;
        }
        // Si se vuelve al catálogo, se baja directamente a las tarjetas
        boolean esCatalogo = volver.equals("/") || volver.startsWith("/?");
        return "redirect:" + volver + (esCatalogo ? "#catalogo" : "");
    }
}