package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConversacion estado = EstadoConversacion.CONSULTA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime ultimaActividad = LocalDateTime.now();

    // Resumen del último mensaje, para los listados sin cargar todo el hilo
    @Column(length = 140)
    private String ultimoResumen;

    private boolean sinLeerUsuario;    // la guarida ha escrito y el usuario aún no lo ha abierto
    private boolean sinLeerGuarida;    // el usuario ha escrito y la guarida aún no lo ha abierto

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC, id ASC")
    private List<Mensaje> mensajes = new ArrayList<>();

    public void addMensaje(Mensaje mensaje) {
        mensaje.setConversacion(this);
        mensajes.add(mensaje);
    }
}