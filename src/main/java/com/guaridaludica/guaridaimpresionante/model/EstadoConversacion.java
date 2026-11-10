package com.guaridaludica.guaridaimpresionante.model;

import java.util.ArrayList;
import java.util.List;

public enum EstadoConversacion {
    CONSULTA("Consulta"),
    SOLICITADA("Solicitada"),
    ACEPTADA("Aceptada"),
    EN_IMPRESION("En impresión"),
    LISTA("Lista"),
    ENTREGADA("Entregada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoConversacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    // Estados a los que se puede pasar desde este
    public List<EstadoConversacion> siguientes() {
        switch (this) {
            case CONSULTA:
                return List.of(SOLICITADA, CANCELADA);
            case SOLICITADA:
                return List.of(ACEPTADA, CANCELADA);
            case ACEPTADA:
                return List.of(EN_IMPRESION, CANCELADA);
            case EN_IMPRESION:
                return List.of(LISTA, CANCELADA);
            case LISTA:
                return List.of(ENTREGADA, CANCELADA);
            default:
                return List.of();
        }
    }

    public boolean puedePasarA(EstadoConversacion destino) {
        return siguientes().contains(destino);
    }

    // El equipo no puede solicitar por el usuario: eso lo decide él
    public List<EstadoConversacion> siguientesParaEquipo() {
        List<EstadoConversacion> lista = new ArrayList<>();
        for (EstadoConversacion e : siguientes()) {
            if (e != SOLICITADA) {
                lista.add(e);
            }
        }
        return lista;
    }

    public boolean isUsuarioPuedeSolicitar() {
        return this == CONSULTA;
    }

    // El usuario puede cancelar hasta que empiece la impresión
    public boolean isUsuarioPuedeCancelar() {
        return this == CONSULTA || this == SOLICITADA || this == ACEPTADA;
    }

    public boolean usuarioPuedePasarA(EstadoConversacion destino) {
        if (destino == SOLICITADA) {
            return isUsuarioPuedeSolicitar();
        }
        if (destino == CANCELADA) {
            return isUsuarioPuedeCancelar();
        }
        return false;
    }

    public boolean isCerrada() {
        return this == ENTREGADA || this == CANCELADA;
    }
}