package com.guaridaludica.guaridaimpresionante.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.guaridaludica.guaridaimpresionante.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByConversacionId(Long conversacionId);

    @Query("""
            SELECT v FROM Venta v
            LEFT JOIN v.producto p
            WHERE (:origen IS NULL OR v.origen = :origen)
              AND (:patron IS NULL OR LOWER(v.concepto) LIKE :patron
                   OR (p IS NOT NULL AND LOWER(p.nombre) LIKE :patron))
            """)
    Page<Venta> buscar(@Param("patron") String patron,
                       @Param("origen") String origen,
                       Pageable pageable);

    @Query("SELECT COALESCE(SUM(v.precioTotal), 0) FROM Venta v")
    BigDecimal sumarIngresosTotales();

    @Query("SELECT COALESCE(SUM(v.costeEstimado), 0) FROM Venta v")
    BigDecimal sumarCostesTotales();

    @Query("SELECT COALESCE(SUM(v.unidades), 0) FROM Venta v")
    Long sumarUnidadesTotales();
}