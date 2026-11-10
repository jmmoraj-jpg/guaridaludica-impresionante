package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @EntityGraph(attributePaths = "imagenes")
    List<Producto> findByVisibleTrue();

    @EntityGraph(attributePaths = "imagenes")
    @Query("""
           select distinct p from Producto p join p.categorias c
           where p.visible = true and c.slug = :slug and c.visible = true
           """)
    List<Producto> buscarPorCategoria(@Param("slug") String slug);

    @EntityGraph(attributePaths = {"imagenes", "categorias"})
    List<Producto> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"imagenes", "categorias"})
    Optional<Producto> findWithImagenesById(Long id);

    @Query("select p from Producto p join p.categorias c where c.id = :id")
    List<Producto> conCategoria(@Param("id") Long id);
}