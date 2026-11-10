package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.controller.ProductoForm;
import com.guaridaludica.guaridaimpresionante.model.ImagenProducto;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.CategoriaRepository;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.repository.FavoritoRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoAdminService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ConversacionRepository conversacionRepository;
    private final FavoritoRepository favoritoRepository;
    private final CloudinaryService cloudinary;

    public ProductoAdminService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
            ConversacionRepository conversacionRepository, FavoritoRepository favoritoRepository, CloudinaryService cloudinary) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.conversacionRepository = conversacionRepository;
        this.favoritoRepository = favoritoRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public Producto guardar(ProductoForm form, List<MultipartFile> nuevas,
            List<Long> eliminar, String portada) {

        Producto p = (form.getId() == null)
                ? new Producto()
                : productoRepository.findWithImagenesById(form.getId()).orElseThrow();

        p.setNombre(form.getNombre().trim());
        p.setDescripcion(vacioANulo(form.getDescripcion()));
        p.getCategorias().clear();
        if (form.getCategoriaIds() != null && !form.getCategoriaIds().isEmpty()) {
            p.getCategorias().addAll(categoriaRepository.findAllById(form.getCategoriaIds()));
        }
        p.setMaterial(vacioANulo(form.getMaterial()));
        p.setTiempoImpresionMinutos(form.getTiempoImpresionMinutos());
        p.setVisible(form.isVisible());

        // 1. Imágenes marcadas para eliminar
        if (eliminar != null && !eliminar.isEmpty()) {
            List<ImagenProducto> aBorrar = p.getImagenes().stream()
                    .filter(i -> eliminar.contains(i.getId()))
                    .toList();
            aBorrar.forEach(i -> cloudinary.borrar(i.getPublicId()));
            p.getImagenes().removeAll(aBorrar);
        }

        // 2. Imágenes nuevas (se guarda la posición de cada una para poder elegirla
        // como portada)
        List<ImagenProducto> porIndice = new ArrayList<>();
        int orden = p.getImagenes().stream().mapToInt(ImagenProducto::getOrden).max().orElse(-1) + 1;
        if (nuevas != null) {
            for (MultipartFile archivo : nuevas) {
                String tipo = archivo.getContentType();
                if (archivo.isEmpty() || tipo == null || !tipo.startsWith("image/")) {
                    porIndice.add(null);
                    continue;
                }
                try {
                    CloudinaryService.ImagenSubida subida = cloudinary.subir(archivo);
                    ImagenProducto img = new ImagenProducto();
                    img.setUrl(subida.url());
                    img.setPublicId(subida.publicId());
                    img.setOrden(orden++);
                    p.addImagen(img);
                    porIndice.add(img);
                } catch (IOException e) {
                    throw new UncheckedIOException("No se pudo subir la imagen", e);
                }
            }
        }

        // 3. Portada
        ImagenProducto elegida = resolverPortada(portada, p, porIndice);
        if (elegida != null) {
            p.getImagenes().forEach(i -> i.setPortada(false));
            elegida.setPortada(true);
        } else if (!p.getImagenes().isEmpty()
                && p.getImagenes().stream().noneMatch(ImagenProducto::isPortada)) {
            p.getImagenes().get(0).setPortada(true);
        }

        return productoRepository.save(p);
    }

    @Transactional
    public void cambiarVisibilidad(Long id) {
        productoRepository.findById(id).ifPresent(p -> p.setVisible(!p.isVisible()));
    }

    @Transactional
    public boolean borrar(Long id) {
        if (conversacionRepository.existsByProductoId(id)) {
            return false; // con conversaciones no se borra: se puede ocultar
        }
        favoritoRepository.deleteByProductoId(id);
        productoRepository.findWithImagenesById(id).ifPresent(p -> {
            p.getImagenes().forEach(i -> cloudinary.borrar(i.getPublicId()));
            productoRepository.delete(p);
        });
        return true;
    }

    // "nueva-2" = tercer archivo subido ahora; "15" = imagen ya existente con id 15
    private ImagenProducto resolverPortada(String portada, Producto p, List<ImagenProducto> porIndice) {
        if (portada == null || portada.isBlank()) {
            return null;
        }
        try {
            if (portada.startsWith("nueva-")) {
                int idx = Integer.parseInt(portada.substring("nueva-".length()));
                return (idx >= 0 && idx < porIndice.size()) ? porIndice.get(idx) : null;
            }
            Long id = Long.valueOf(portada);
            return p.getImagenes().stream()
                    .filter(i -> id.equals(i.getId()))
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String vacioANulo(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}