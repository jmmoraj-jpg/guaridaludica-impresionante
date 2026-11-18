package com.guaridaludica.guaridaimpresionante.controller;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.model.Producto;

import jakarta.validation.constraints.DecimalMin;
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

    @Min(value = 0, message = "No puede ser negativo")
    private Integer tiempoImpresionMinutos;

    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double altoCm;

    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double anchoCm;

    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double fondoCm;

    private boolean visible = true;

    private boolean novedad;

    private Double costeProduccion;
    private Double margenBeneficioAplicado;
    private Double precioSugerido;

    // Lista para recibir los filamentos y sus gramos desde la vista de
    // administración
    private List<FilamentoAsignadoForm> filamentosForm = new ArrayList<>();

    @Getter
    @Setter
    public static class FilamentoAsignadoForm {
        private Long filamentoId;
        private Double gramos = 0.0;
    }

    public static ProductoForm desde(Producto p) {
        ProductoForm f = new ProductoForm();
        f.setId(p.getId());
        f.setNombre(p.getNombre());
        f.setDescripcion(p.getDescripcion());
        f.setCategoriaIds(p.getCategorias().stream().map(Categoria::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        f.setTiempoImpresionMinutos(p.getTiempoImpresionMinutos());
        f.setAltoCm(p.getAltoCm());
        f.setAnchoCm(p.getAnchoCm());
        f.setFondoCm(p.getFondoCm());
        f.setVisible(p.isVisible());
        f.setNovedad(p.isNovedad());
        // Copiar los datos económicos calculados
        f.setCosteProduccion(p.getCosteProduccion());
        f.setMargenBeneficioAplicado(p.getMargenBeneficioAplicado());
        f.setPrecioSugerido(p.getPrecioSugerido());
        
        // Mapear los filamentos usados actuales a las filas del formulario
        if (p.getFilamentosUsados() != null) {
            f.filamentosForm = p.getFilamentosUsados().stream().map(pf -> {
                FilamentoAsignadoForm faf = new FilamentoAsignadoForm();
                faf.setFilamentoId(pf.getFilamento().getId());
                faf.setGramos(pf.getGramos());
                return faf;
            }).collect(Collectors.toList());
        }
        return f;
    }
}