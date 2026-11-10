package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.controller.CategoriaForm;
import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.repository.CategoriaRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public boolean nombreEnUso(String nombre, Long idExcluido) {
        return categoriaRepository.findByNombreIgnoreCase(nombre.trim())
                .filter(c -> !c.getId().equals(idExcluido))
                .isPresent();
    }

    @Transactional
    public Categoria guardar(CategoriaForm form) {
        Categoria c;
        if (form.getId() == null) {
            c = new Categoria();
            c.setSlug(slugUnico(form.getNombre()));
        } else {
            c = categoriaRepository.findById(form.getId()).orElseThrow();
        }
        c.setNombre(form.getNombre().trim());
        c.setVisible(form.isVisible());
        if (form.getOrden() != null) {
            c.setOrden(form.getOrden());
        } else if (form.getId() == null) {
            c.setOrden(categoriaRepository.maxOrden() + 1);
        }
        return categoriaRepository.save(c);
    }

    @Transactional
    public void cambiarVisibilidad(Long id) {
        categoriaRepository.findById(id).ifPresent(c -> c.setVisible(!c.isVisible()));
    }

    // "Decoración y regalos" -> "decoracion-y-regalos" (con sufijo -2, -3... si ya
    // existe)
    private String slugUnico(String nombre) {
        String base = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) {
            base = "categoria";
        }
        String slug = base;
        int n = 2;
        while (categoriaRepository.existsBySlug(slug)) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    @Transactional
    public Categoria obtenerOCrear(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre.trim()).orElseGet(() -> {
            Categoria c = new Categoria();
            c.setNombre(nombre.trim());
            c.setSlug(slugUnico(nombre));
            c.setOrden(categoriaRepository.maxOrden() + 1);
            return categoriaRepository.save(c);
        });
    }

    // Quita la categoría de las piezas que la tengan y luego la borra
    @Transactional
    public void borrar(Long id) {
        productoRepository.conCategoria(id)
                .forEach(p -> p.getCategorias().removeIf(c -> c.getId().equals(id)));
        categoriaRepository.deleteById(id);
    }
}