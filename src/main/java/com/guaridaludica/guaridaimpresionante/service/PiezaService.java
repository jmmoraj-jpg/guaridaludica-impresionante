package com.guaridaludica.guaridaimpresionante.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guaridaludica.guaridaimpresionante.model.Categoria;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PiezaService {

    private static final int MAX_RELACIONADAS = 4;

    private final ProductoRepository productoRepository;

    public PiezaService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> buscarRelacionadas(Producto pieza) {
        Set<Long> idsCategorias = pieza.getCategorias().stream()
                .filter(Categoria::isVisible)
                .map(Categoria::getId)
                .collect(Collectors.toSet());

        List<Producto> candidatas = productoRepository.findByVisibleTrue(
                PageRequest.of(0, 24, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent().stream()
                .filter(p -> !p.getId().equals(pieza.getId()))
                .toList();

        Map<Long, Producto> seleccionadas = new LinkedHashMap<>();

        if (!idsCategorias.isEmpty()) {
            candidatas.stream()
                    .filter(p -> p.getCategorias().stream()
                            .filter(Categoria::isVisible)
                            .anyMatch(c -> idsCategorias.contains(c.getId())))
                    .limit(MAX_RELACIONADAS)
                    .forEach(p -> seleccionadas.putIfAbsent(p.getId(), p));
        }

        if (seleccionadas.size() < MAX_RELACIONADAS) {
            for (Producto p : candidatas) {
                if (seleccionadas.size() >= MAX_RELACIONADAS) {
                    break;
                }
                seleccionadas.putIfAbsent(p.getId(), p);
            }
        }

        return new ArrayList<>(seleccionadas.values());
    }
}