package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "imagenes_producto")
@Getter
@Setter
@NoArgsConstructor
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false, length = 300)
    private String publicId;

    private int orden;

    private boolean portada;

    public String getUrlGrande() {
        return transformar("f_auto,q_auto,w_1200");
    }

    public String getUrlMiniatura() {
        return transformar("f_auto,q_auto,w_600");
    }

    private String transformar(String t) {
        return url.replace("/upload/", "/upload/" + t + "/");
    }
}