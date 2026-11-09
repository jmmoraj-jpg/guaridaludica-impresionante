package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.model.Favorito;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.FavoritoRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public FavoritoService(FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    // Si ya era favorito lo quita; si no, lo añade
    @Transactional
    public void alternar(String email, Long productoId) {
        var existente = favoritoRepository.findByUsuarioEmailIgnoreCaseAndProductoId(email, productoId);
        if (existente.isPresent()) {
            favoritoRepository.delete(existente.get());
            return;
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::isVisible)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Favorito f = new Favorito();
        f.setUsuario(usuario);
        f.setProducto(producto);
        favoritoRepository.save(f);
    }

    @Transactional(readOnly = true)
    public List<Producto> productosFavoritos(String email) {
        return favoritoRepository.findByUsuarioEmailIgnoreCaseOrderByFechaDesc(email).stream()
                .map(Favorito::getProducto)
                .filter(Producto::isVisible)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<Long> idsFavoritos(String email) {
        return favoritoRepository.idsProducto(email);
    }
}