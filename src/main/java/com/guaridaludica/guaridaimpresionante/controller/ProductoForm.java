package com.guaridaludica.guaridaimpresionante.controller;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    private String nombre;

    @Size(max = 1000, message = "Máximo 1000 caracteres")
    private String descripcion;

    private Set<Long> categoriaIds = new LinkedHashSet<>();

    @Size(max = 50)
    private String material;

    @Min(value = 0, message = "No puede ser negativo")
    private Integer tiempoImpresionMinutos;

    private boolean visible = true;

    public static ProductoForm desde(Producto p) {
        ProductoForm f = new ProductoForm();
        f.setId(p.getId());
        f.setNombre(p.getNombre());
        f.setDescripcion(p.getDescripcion());
        f.setCategoriaIds(p.getCategorias().stream().map(Categoria::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        f.setMaterial(p.getMaterial());
        f.setTiempoImpresionMinutos(p.getTiempoImpresionMinutos());
        f.setVisible(p.isVisible());
        return f;
    }
}