package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "producto_filamento", schema = "guarida3d")
@Getter
@Setter
public class ProductoFilamento {

    @EmbeddedId
    private ProductoFilamentoId id = new ProductoFilamentoId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("filamentoId")
    @JoinColumn(name = "filamento_id")
    private Filamento filamento;

    @Column(nullable = false)
    private Double gramos = 0.0;
}