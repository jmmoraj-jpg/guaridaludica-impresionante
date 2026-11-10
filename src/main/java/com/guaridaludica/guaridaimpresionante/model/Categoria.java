package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    // Identificador para la URL (/?categoria=figuras). No cambia al renombrar, para no romper enlaces
    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    private boolean visible = true;

    private int orden;
}