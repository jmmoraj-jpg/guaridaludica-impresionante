package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String nombre;

    @Min(value = 0, message = "No puede ser negativo")
    private Integer orden;

    private boolean visible = true;

    public static CategoriaForm desde(Categoria c) {
        CategoriaForm f = new CategoriaForm();
        f.setId(c.getId());
        f.setNombre(c.getNombre());
        f.setOrden(c.getOrden());
        f.setVisible(c.isVisible());
        return f;
    }
}