package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "producto_id", nullable = true)
    private Producto producto;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "conversacion_id", nullable = true)
    private Conversacion conversacion;

    @Column(nullable = false, length = 160)
    private String concepto;

    @Column(nullable = false)
    private Integer unidades = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costeEstimado = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String origen = "MANUAL"; // "CONVERSACION" o "MANUAL"

    public BigDecimal getBeneficioEstimado() {
        BigDecimal precio = precioTotal != null ? precioTotal : BigDecimal.ZERO;
        BigDecimal coste = costeEstimado != null ? costeEstimado : BigDecimal.ZERO;
        return precio.subtract(coste);
    }
}