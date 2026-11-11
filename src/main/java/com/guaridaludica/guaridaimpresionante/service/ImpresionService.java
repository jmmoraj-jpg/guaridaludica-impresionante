package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.model.Impresion;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.ImpresionRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImpresionService {

    public static final int MAX = 4;

    private final ImpresionRepository impresionRepository;
    private final ProductoRepository productoRepository;

    public ImpresionService(ImpresionRepository impresionRepository,
                            ProductoRepository productoRepository) {
        this.impresionRepository = impresionRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Impresion> enCurso() {
        return impresionRepository.todasConProducto();
    }

    // Devuelve un mensaje de error, o null si todo ha ido bien
    @Transactional
    public String anadir(Long productoId, int progreso) {
        if (impresionRepository.count() >= MAX) {
            return "Ya hay " + MAX + " impresiones en curso. Quita alguna antes de añadir otra.";
        }
        if (impresionRepository.existsByProductoId(productoId)) {
            return "Esa pieza ya está en la lista.";
        }
        Producto p = productoRepository.findById(productoId).orElse(null);
        if (p == null) {
            return "No se encuentra esa pieza.";
        }
        Impresion i = new Impresion();
        i.setProducto(p);
        i.setProgreso(limitar(progreso));
        impresionRepository.save(i);
        return null;
    }

    @Transactional
    public void actualizar(Long id, int progreso) {
        Impresion i = impresionRepository.findById(id).orElse(null);
        if (i != null) {
            i.setProgreso(limitar(progreso));
        }
    }

    @Transactional
    public void quitar(Long id) {
        impresionRepository.deleteById(id);
    }

    private int limitar(int progreso) {
        if (progreso < 0) {
            return 0;
        }
        if (progreso > 100) {
            return 100;
        }
        return progreso;
    }
}