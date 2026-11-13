package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Direccion;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUsuario(Usuario usuario);
    Optional<Direccion> findByIdAndUsuario(Long id, Usuario usuario);
}