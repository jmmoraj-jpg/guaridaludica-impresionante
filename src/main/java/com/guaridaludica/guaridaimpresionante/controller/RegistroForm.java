package com.guaridaludica.guaridaimpresionante.controller;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroForm {

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 80)
    private String nombre;

    @NotBlank(message = "Escribe tu email")
    @Email(message = "El email no es válido")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "Escribe una contraseña")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    @NotBlank(message = "Repite la contraseña")
    private String confirmarPassword;

    @AssertTrue(message = "Debes aceptar la política de privacidad")
    private boolean aceptaPrivacidad;
}