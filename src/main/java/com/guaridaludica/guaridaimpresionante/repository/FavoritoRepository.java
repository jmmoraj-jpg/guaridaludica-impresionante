package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Favorito;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    Optional<Favorito> findByUsuarioEmailIgnoreCaseAndProductoId(String email, Long productoId);

    boolean existsByUsuarioEmailIgnoreCaseAndProductoId(String email, Long productoId);

    @EntityGraph(attributePaths = { "producto", "producto.imagenes" })
    List<Favorito> findByUsuarioEmailIgnoreCaseOrderByFechaDesc(String email);

    @Query("select f.producto.id from Favorito f where lower(f.usuario.email) = lower(:email)")
    Set<Long> idsProducto(@Param("email") String email);

    void deleteByProductoId(Long productoId);
}