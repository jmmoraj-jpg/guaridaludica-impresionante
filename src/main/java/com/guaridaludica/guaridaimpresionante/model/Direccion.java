package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "direcciones", schema = "guarida3d")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    @Column(nullable = false)
    private String nombreDestinatario;

    @NotBlank(message = "La calle y número son obligatorios")
    @Column(nullable = false)
    private String calle;

    private String piso; // Opcional (piso, puerta, escalera)

    @NotBlank(message = "La ciudad es obligatoria")
    @Column(nullable = false)
    private String ciudad;

    @NotBlank(message = "El código postal es obligatorio")
    @Column(nullable = false, length = 10)
    private String codigoPostal;

    @NotBlank(message = "La provincia es obligatoria")
    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private boolean principal = false; // Para marcar la dirección predeterminada
}