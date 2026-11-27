package com.guaridaludica.guaridaimpresionante.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.SuscripcionPush;
import com.guaridaludica.guaridaimpresionante.repository.SuscripcionPushRepository;

import jakarta.transaction.Transactional;

@Service
public class PushNotificationService {

    private final SuscripcionPushRepository suscripcionPushRepository;
    private final String publicKey;
    private final String privateKey;
    // private final String subject;

    public PushNotificationService(
            SuscripcionPushRepository suscripcionPushRepository,
            @Value("${app.push.vapid.public-key:}") String publicKey,
            @Value("${app.push.vapid.private-key:}") String privateKey,
            @Value("${app.push.vapid.subject:mailto:laguaridaludica@gmail.com}") String subject) {
        this.suscripcionPushRepository = suscripcionPushRepository;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        // this.subject = subject;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public boolean estaConfigurado() {
        return publicKey != null && !publicKey.isBlank()
                && privateKey != null && !privateKey.isBlank();
    }

    @Transactional
    public void notificarNuevaActividadConversacion(Conversacion conv, boolean enviadoPorGuarida, String titulo, String cuerpo) {
        if (!estaConfigurado() || conv == null || conv.getId() == null) {
            return;
        }
        List<SuscripcionPush> destinos = enviadoPorGuarida
                ? suscripcionPushRepository.findByUsuarioId(conv.getUsuario().getId())
                : suscripcionPushRepository.findByEsAdminTrue();

        String urlDestino = enviadoPorGuarida
                ? "/conversaciones/" + conv.getId()
                : "/admin/mensajes/" + conv.getId();

        for (SuscripcionPush sub : destinos) {
            enviarASuscripcion(sub, titulo, cuerpo, urlDestino);
        }
    }

    private void enviarASuscripcion(SuscripcionPush sub, String titulo, String cuerpo, String urlDestino) {
        // Aquí se invoca el envío Web Push con PushService de nl.martijndwars:web-push
        // Si el proveedor push responde 404 o 410 (suscripción caducada), borrar con:
        // suscripcionPushRepository.deleteByEndpoint(sub.getEndpoint());
    }
}