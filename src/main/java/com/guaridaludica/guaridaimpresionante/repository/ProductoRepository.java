package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByVisibleTrue(Pageable pageable);

    @Query(value = """
                   select distinct p from Producto p join p.categorias c
                   where p.visible = true and c.slug = :slug and c.visible = true
                   """,
           countQuery = """
                   select count(distinct p) from Producto p join p.categorias c
                   where p.visible = true and c.slug = :slug and c.visible = true
                   """)
    Page<Producto> buscarPorCategoria(@Param("slug") String slug, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    @EntityGraph(attributePaths = {"imagenes", "categorias"})
    Optional<Producto> findWithImagenesById(Long id);

    @Query("select p from Producto p join p.categorias c where c.id = :id")
    List<Producto> conCategoria(@Param("id") Long id);
}