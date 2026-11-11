package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Impresion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImpresionRepository extends JpaRepository<Impresion, Long> {

    @Query("select i from Impresion i join fetch i.producto order by i.id")
    List<Impresion> todasConProducto();

    boolean existsByProductoId(Long productoId);
}