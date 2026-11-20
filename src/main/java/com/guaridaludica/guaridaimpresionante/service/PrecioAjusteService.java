package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.model.ProductoFilamento;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrecioAjusteService {

    // Parámetros base alineados con la calculadora de taller
    private static final double COSTE_HORA_DEFECTO = 0.25;
    private static final double FACTOR_RIESGO_DEFECTO = 10.0; // %
    private static final double MARGEN_DEFECTO = 100.0; // %

    private final ProductoRepository productoRepository;

    public PrecioAjusteService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional
    public void recalcularProducto(Producto producto) {
        if (producto == null) {
            return;
        }

        // 1. Coste de filamentos (igual que en calculadora.js)
        double costeMaterial = 0.0;
        if (producto.getFilamentosUsados() != null) {
            for (ProductoFilamento pf : producto.getFilamentosUsados()) {
                double gramos = pf.getGramos() != null ? pf.getGramos() : 0.0;
                double precioKg = (pf.getFilamento() != null && pf.getFilamento().getPrecioPorKg() != null)
                        ? pf.getFilamento().getPrecioPorKg()
                        : 20.0;
                costeMaterial += (gramos / 1000.0) * precioKg;
            }
        }

        // 2. Energía y desgaste
        int minutos = producto.getTiempoImpresionMinutos() != null ? producto.getTiempoImpresionMinutos() : 0;
        double costeEnergia = (minutos / 60.0) * COSTE_HORA_DEFECTO;

        // 3. Riesgo / fallos sobre (material + energía)
        double subtotalBase = costeMaterial + costeEnergia;
        double costeRiesgo = subtotalBase * (FACTOR_RIESGO_DEFECTO / 100.0);

        // 4. Coste real de producción
        double costeTotal = subtotalBase + costeRiesgo;

        // 5. Margen de beneficio y redondeo a 0,50 € (igual que en calculadora.js)
        double margen = producto.getMargenBeneficioAplicado() != null
                ? producto.getMargenBeneficioAplicado()
                : MARGEN_DEFECTO;

        double precioExacto = costeTotal * (1.0 + (margen / 100.0));
        double precioSugerido = Math.ceil(precioExacto * 2.0) / 2.0;

        producto.setCosteProduccion(redondearDosDecimales(costeTotal));
        producto.setMargenBeneficioAplicado(margen);
        producto.setPrecioSugerido(precioSugerido);

        productoRepository.saveAndFlush(producto);
    }

    @Transactional
    public void recalcularPorFilamento(Long filamentoId) {
        if (filamentoId == null) {
            return;
        }
        List<Producto> afectados = productoRepository.buscarPorFilamentoId(filamentoId);
        for (Producto p : afectados) {
            recalcularProducto(p);
        }
    }
    
    private double redondearDosDecimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}