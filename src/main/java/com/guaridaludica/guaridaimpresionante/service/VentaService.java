package com.guaridaludica.guaridaimpresionante.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.model.Venta;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.repository.VentaRepository;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public void registrarDesdeConversacionEntregada(Conversacion conv) {
        if (conv == null || conv.getId() == null) {
            return;
        }
        if (ventaRepository.existsByConversacionId(conv.getId())) {
            return;
        }

        Producto producto = conv.getProducto();
        String concepto;
        BigDecimal costeUnitario = BigDecimal.ZERO;

        if (producto != null) {
            concepto = producto.getNombre();
            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            if (stockActual > 0) {
                producto.setStock(stockActual - 1);
                productoRepository.save(producto);
            }
            if (producto.getCosteProduccion() != null) {
                costeUnitario = BigDecimal.valueOf(producto.getCosteProduccion())
                        .setScale(2, RoundingMode.HALF_UP);
            }
        } else {
            concepto = (conv.getResumenEncargo() != null && !conv.getResumenEncargo().isBlank())
                    ? conv.getResumenEncargo()
                    : (conv.getUltimoResumen() != null ? conv.getUltimoResumen() : "Encargo personalizado");
        }

        BigDecimal precio = conv.getPrecio() != null
                ? conv.getPrecio().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setProducto(producto);
        venta.setConversacion(conv);
        venta.setConcepto(concepto);
        venta.setUnidades(1);
        venta.setPrecioTotal(precio);
        venta.setCosteEstimado(costeUnitario);
        venta.setOrigen("CONVERSACION");

        ventaRepository.save(venta);
    }

    @Transactional
    public void registrarVentaManual(Long productoId, String conceptoManual, int unidades,
            BigDecimal precioTotal, boolean descontarStock) {
        int uds = Math.max(1, unidades);
        Producto producto = null;
        String conceptoFinal = (conceptoManual != null && !conceptoManual.isBlank())
                ? conceptoManual.trim()
                : "Venta directa";
        BigDecimal costeUnitario = BigDecimal.ZERO;

        if (productoId != null) {
            producto = productoRepository.findById(productoId).orElse(null);
            if (producto != null) {
                if (conceptoManual == null || conceptoManual.isBlank()) {
                    conceptoFinal = producto.getNombre();
                }
                if (producto.getCosteProduccion() != null) {
                    costeUnitario = BigDecimal.valueOf(producto.getCosteProduccion())
                            .setScale(2, RoundingMode.HALF_UP);
                }
                if (descontarStock) {
                    int stockActual = producto.getStock() != null ? producto.getStock() : 0;
                    int nuevoStock = Math.max(0, stockActual - uds);
                    producto.setStock(nuevoStock);
                    productoRepository.save(producto);
                }
            }
        }

        BigDecimal precioFinal = precioTotal != null
                ? precioTotal.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setProducto(producto);
        venta.setConcepto(conceptoFinal);
        venta.setUnidades(uds);
        venta.setPrecioTotal(precioFinal);
        venta.setCosteEstimado(costeUnitario.multiply(BigDecimal.valueOf(uds)).setScale(2, RoundingMode.HALF_UP));
        venta.setOrigen("MANUAL");

        ventaRepository.save(venta);
    }
}