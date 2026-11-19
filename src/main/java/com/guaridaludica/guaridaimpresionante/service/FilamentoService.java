package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.controller.FilamentoForm;
import com.guaridaludica.guaridaimpresionante.model.Filamento;
import com.guaridaludica.guaridaimpresionante.repository.FilamentoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FilamentoService {

    private final FilamentoRepository filamentoRepository;
    private final PrecioAjusteService precioAjusteService;

    public FilamentoService(FilamentoRepository filamentoRepository, PrecioAjusteService precioAjusteService) {
        this.filamentoRepository = filamentoRepository;
        this.precioAjusteService = precioAjusteService;
    }

    public boolean nombreEnUso(String nombre, Long id) {
        Filamento existente = filamentoRepository.findByNombreIgnoreCase(nombre);
        if (existente == null) {
            return false;
        }
        // Si estamos editando y el ID coincide, no es un duplicado propio
        return id == null || !existente.getId().equals(id);
    }

    public void guardar(FilamentoForm form) {
        Filamento filamento;
        if (form.getId() != null) {
            filamento = filamentoRepository.findById(form.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            filamento.setNombre(form.getNombre());
            filamento.setColorHex(form.getColorHex());
            filamento.setActivo(form.isActivo());
            filamento.setPrecioPorKg(form.getPrecioPorKg()); // <-- ¡Faltaba aquí al editar!
        } else {
            filamento = new Filamento();
            filamento.setNombre(form.getNombre());
            filamento.setColorHex(form.getColorHex());
            filamento.setActivo(form.isActivo());
            filamento.setPrecioPorKg(form.getPrecioPorKg());
        }
        Filamento guardado = filamentoRepository.save(filamento);
        precioAjusteService.recalcularPorFilamento(guardado.getId());
    }

    public void cambiarEstado(Long id) {
        Filamento f = filamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        f.setActivo(!f.isActivo());
        filamentoRepository.save(f);
    }

    public void borrar(Long id) {
        if (!filamentoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        filamentoRepository.deleteById(id);
    }
}