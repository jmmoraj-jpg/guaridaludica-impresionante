package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversaciones")
@Getter
@Setter
@NoArgsConstructor
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Ahora es opcional (nullable = true) para encargos libres
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "producto_id", nullable = true)
    private Producto producto;

    // Nuevos campos para ideas personalizadas
    @Column(name = "imagen1")
    private String imagen1;

    @Column(name = "imagen1_public_id")
    private String imagen1PublicId;

    @Column(name = "imagen2")
    private String imagen2;

    @Column(name = "imagen2_public_id")
    private String imagen2PublicId;

    @Column(name = "imagen3")
    private String imagen3;

    @Column(name = "imagen3_public_id")
    private String imagen3PublicId;

    @Column(length = 500)
    private String enlaceReferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConversacion estado = EstadoConversacion.CONSULTA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime ultimaActividad = LocalDateTime.now();

    @Column(length = 140)
    private String ultimoResumen;

    @Column(precision = 8, scale = 2)
    private BigDecimal precio;

    private boolean sinLeerUsuario;
    private boolean sinLeerGuarida;

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC, id ASC")
    private List<Mensaje> mensajes = new ArrayList<>();

    public void addMensaje(Mensaje mensaje) {
        mensaje.setConversacion(this);
        mensajes.add(mensaje);
    }
}