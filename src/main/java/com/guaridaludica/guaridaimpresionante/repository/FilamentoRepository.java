package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Filamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilamentoRepository extends JpaRepository<Filamento, Long> {
    
    List<Filamento> findByActivoTrue();

    // Para la búsqueda paginada en el admin
    Page<Filamento> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Para comprobar duplicados en el servicio
    Filamento findByNombreIgnoreCase(String nombre);
}