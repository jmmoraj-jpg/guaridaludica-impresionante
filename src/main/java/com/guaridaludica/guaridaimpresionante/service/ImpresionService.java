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

    // Frases divertidas de taller para dar vida a la impresora
    private static final List<String> MENSAJES_TALLER = List.of(
        "Calentando extrusor a 210ºC...",
        "Nivelando la cama caliente con paciencia milimétrica...",
        "Trazando perímetros con mimo y precisión...",
        "Calculando soportes estratégicos para que no sufras...",
        "Vigilando la sagrada primera capa como un halcón...",
        "Fundiendo filamento y dando forma a las ideas..."
    );

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

    // Método para obtener una frase aleatoria del taller
    public String obtenerMensajeTaller() {
        if (MENSAJES_TALLER.isEmpty()) {
            return "Imprimiendo sin pausa...";
        }
        int index = new java.util.Random().nextInt(MENSAJES_TALLER.size());
        return MENSAJES_TALLER.get(index);
    }
}