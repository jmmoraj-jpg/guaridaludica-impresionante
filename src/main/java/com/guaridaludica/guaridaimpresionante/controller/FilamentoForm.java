package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Filamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilamentoForm {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El color HEX es obligatorio")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Debe ser un código HEX válido (ej. #212529)")
    private String colorHex;

    private boolean activo = true;

    // Constructores
    public FilamentoForm() {}

    public static FilamentoForm desde(Filamento f) {
        FilamentoForm form = new FilamentoForm();
        form.setId(f.getId());
        form.setNombre(f.getNombre());
        form.setColorHex(f.getColorHex());
        form.setActivo(f.isActivo());
        return form;
    }
    
}
