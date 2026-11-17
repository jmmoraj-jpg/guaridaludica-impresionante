package com.guaridaludica.guaridaimpresionante.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class ProductoFilamentoId implements Serializable {
    private Long productoId;
    private Long filamentoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductoFilamentoId)) return false;
        ProductoFilamentoId that = (ProductoFilamentoId) o;
        return Objects.equals(productoId, that.productoId) && Objects.equals(filamentoId, that.filamentoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId, filamentoId);
    }
}