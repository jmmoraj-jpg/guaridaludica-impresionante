package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @EntityGraph(attributePaths = "imagenes")
    List<Producto> findByVisibleTrue();

    @EntityGraph(attributePaths = "imagenes")
    List<Producto> findByVisibleTrueAndCategoria(String categoria);

    @EntityGraph(attributePaths = "imagenes")
    List<Producto> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "imagenes")
    Optional<Producto> findWithImagenesById(Long id);
}