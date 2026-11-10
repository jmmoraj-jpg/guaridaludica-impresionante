package com.guaridaludica.guaridaimpresionante.model;

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
}