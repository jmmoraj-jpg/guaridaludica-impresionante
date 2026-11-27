package com.guaridaludica.guaridaimpresionante.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guaridaludica.guaridaimpresionante.model.SuscripcionPush;

public interface SuscripcionPushRepository extends JpaRepository<SuscripcionPush, Long> {
    Optional<SuscripcionPush> findByEndpoint(String endpoint);
    List<SuscripcionPush> findByUsuarioId(Long usuarioId);
    List<SuscripcionPush> findByEsAdminTrue();
    void deleteByEndpoint(String endpoint);
}