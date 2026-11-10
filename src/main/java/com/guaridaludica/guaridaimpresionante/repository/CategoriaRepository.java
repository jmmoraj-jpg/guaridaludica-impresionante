package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByOrdenAscNombreAsc();

    Optional<Categoria> findBySlug(String slug);

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsBySlug(String slug);

    @Query("select coalesce(max(c.orden), 0) from Categoria c")
    Integer maxOrden();

    @Query("""
            select c from Categoria c
            where c.visible = true
              and exists (select 1 from Producto p join p.categorias pc
                          where pc.id = c.id and p.visible = true)
            order by c.orden asc, c.nombre asc
            """)
    List<Categoria> visiblesConPiezas();
}