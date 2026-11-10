package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import org.springframework.stereotype.Component;

@Component("avisos")
public class Avisos {

    private final ConversacionRepository conversacionRepository;

    public Avisos(ConversacionRepository conversacionRepository) {
        this.conversacionRepository = conversacionRepository;
    }

    // Conversaciones donde la guarida ha escrito y el usuario aún no lo ha abierto
    public long deUsuario(String email) {
        return conversacionRepository.countByUsuarioEmailIgnoreCaseAndSinLeerUsuarioTrue(email);
    }

    // Conversaciones donde un usuario ha escrito y el equipo aún no lo ha abierto
    public long deEquipo() {
        return conversacionRepository.countBySinLeerGuaridaTrue();
    }
}