package com.guaridaludica.guaridaimpresionante.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.SuscripcionPush;
import com.guaridaludica.guaridaimpresionante.repository.SuscripcionPushRepository;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final SuscripcionPushRepository suscripcionPushRepository;
    private final String publicKey;
    private final String privateKey;
    private final String subject;

    public PushNotificationService(
            SuscripcionPushRepository suscripcionPushRepository,
            @Value("${app.push.vapid.public-key:}") String publicKey,
            @Value("${app.push.vapid.private-key:}") String privateKey,
            @Value("${app.push.vapid.subject:mailto:laguaridaludica@gmail.com}") String subject) {
        this.suscripcionPushRepository = suscripcionPushRepository;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.subject = subject;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public boolean estaConfigurado() {
        return publicKey != null && !publicKey.isBlank()
                && privateKey != null && !privateKey.isBlank();
    }

    @Transactional
    public void notificarNuevaActividadConversacion(Conversacion conv, boolean enviadoPorGuarida, String titulo,
            String cuerpo) {
        if (!estaConfigurado() || conv == null || conv.getId() == null) {
            return;
        }

        List<SuscripcionPush> destinos;
        if (enviadoPorGuarida) {
            if (conv.getUsuario() == null || conv.getUsuario().getId() == null) {
                return;
            }
            destinos = suscripcionPushRepository.findByUsuarioId(conv.getUsuario().getId());
        } else {
            destinos = suscripcionPushRepository.findByEsAdminTrue();
        }

        if (destinos == null || destinos.isEmpty()) {
            return;
        }

        String urlDestino = enviadoPorGuarida
                ? "/conversaciones/" + conv.getId()
                : "/admin/mensajes/" + conv.getId();

        String payloadJson = construirPayload(titulo, cuerpo, urlDestino);
        PushService pushService = crearPushService();

        for (SuscripcionPush sub : destinos) {
            enviarASuscripcion(pushService, sub, payloadJson);
        }
    }

    private PushService crearPushService() {
        try {
            PushService service = new PushService();
            service.setPublicKey(publicKey);
            service.setPrivateKey(privateKey);
            service.setSubject(subject);
            return service;
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo inicializar PushService con las claves VAPID", ex);
        }
    }

    private String construirPayload(String titulo, String cuerpo, String urlDestino) {
        String tituloSeguro = escaparJson(titulo != null ? titulo : "La Guarida Lúdica");
        String cuerpoSeguro = escaparJson(cuerpo != null ? cuerpo : "Tienes una novedad en una conversación.");
        String urlSegura = escaparJson(urlDestino);
        return "{\"titulo\":\"" + tituloSeguro + "\",\"cuerpo\":\"" + cuerpoSeguro + "\",\"url\":\"" + urlSegura
                + "\"}";
    }

    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void enviarASuscripcion(PushService pushService, SuscripcionPush sub, String payloadJson) {
        if (sub == null || sub.getEndpoint() == null || sub.getEndpoint().isBlank()) {
            return;
        }
        try {
            Notification notification = new Notification(
                    sub.getEndpoint(),
                    sub.getP256dh(),
                    sub.getAuth(),
                    payloadJson.getBytes(StandardCharsets.UTF_8));
            HttpResponse response = pushService.send(notification);
            int statusCode = response != null && response.getStatusLine() != null
                    ? response.getStatusLine().getStatusCode()
                    : -1;

            if (statusCode == 404 || statusCode == 410) {
                suscripcionPushRepository.deleteByEndpoint(sub.getEndpoint());
                log.info("Suscripción push caducada ({}) eliminada: {}", statusCode, sub.getEndpoint());
            } else if (statusCode < 200 || statusCode >= 300) {
                log.warn("Respuesta no exitosa ({}) al enviar push a {}", statusCode, sub.getEndpoint());
            }
        } catch (Exception ex) {
            log.error("Error al enviar notificación push a {}", sub.getEndpoint(), ex);
        }
    }

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}