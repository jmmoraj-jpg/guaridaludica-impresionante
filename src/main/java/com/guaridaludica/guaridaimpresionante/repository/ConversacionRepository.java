package com.guaridaludica.guaridaimpresionante.repository;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.EstadoConversacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

  @Query("""
      select c from Conversacion c
      where lower(c.usuario.email) = lower(:email)
        and c.producto.id = :productoId
        and c.estado not in :cerradas
      order by c.id desc
      """)
  List<Conversacion> abiertasDe(@Param("email") String email,
      @Param("productoId") Long productoId,
      @Param("cerradas") Collection<EstadoConversacion> cerradas);

  Page<Conversacion> findByUsuarioEmailIgnoreCase(String email, Pageable pageable);

  // Bandeja del equipo: solo conversaciones con algún mensaje
  Page<Conversacion> findByUltimoResumenIsNotNull(Pageable pageable);

  Page<Conversacion> findBySinLeerGuaridaTrue(Pageable pageable);

  boolean existsByProductoId(Long productoId);

  long countByUsuarioEmailIgnoreCaseAndSinLeerUsuarioTrue(String email);

  long countBySinLeerGuaridaTrue();
}