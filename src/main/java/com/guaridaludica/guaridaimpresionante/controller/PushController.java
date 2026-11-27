package com.guaridaludica.guaridaimpresionante.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guaridaludica.guaridaimpresionante.model.Rol;
import com.guaridaludica.guaridaimpresionante.model.SuscripcionPush;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.SuscripcionPushRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import com.guaridaludica.guaridaimpresionante.service.PushNotificationService;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final SuscripcionPushRepository suscripcionPushRepository;
    private final UsuarioRepository usuarioRepository;
    private final PushNotificationService pushNotificationService;

    public PushController(SuscripcionPushRepository suscripcionPushRepository,
                          UsuarioRepository usuarioRepository,
                          PushNotificationService pushNotificationService) {
        this.suscripcionPushRepository = suscripcionPushRepository;
        this.usuarioRepository = usuarioRepository;
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping("/vapid-public-key")
    public ResponseEntity<VapidKeyResponse> vapidPublicKey() {
        String key = pushNotificationService.getPublicKey();
        return ResponseEntity.ok(new VapidKeyResponse(key != null ? key : ""));
    }

    @PostMapping("/suscribir")
    @Transactional
    public ResponseEntity<Void> suscribir(@RequestBody PushSuscripcionRequest req, Principal principal) {
        if (principal == null || req == null
                || isBlank(req.endpoint())
                || isBlank(req.p256dh())
                || isBlank(req.auth())) {
            return ResponseEntity.badRequest().build();
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();
        SuscripcionPush sub = suscripcionPushRepository.findByEndpoint(req.endpoint())
                .orElseGet(SuscripcionPush::new);

        sub.setUsuario(usuario);
        sub.setEndpoint(req.endpoint());
        sub.setP256dh(req.p256dh());
        sub.setAuth(req.auth());
        sub.setEsAdmin(esRolAdmin(usuario));

        suscripcionPushRepository.save(sub);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/desuscribir")
    @Transactional
    public ResponseEntity<Void> desuscribir(@RequestBody Map<String, String> body) {
        String endpoint = body != null ? body.get("endpoint") : null;
        if (!isBlank(endpoint)) {
            suscripcionPushRepository.deleteByEndpoint(endpoint);
        }
        return ResponseEntity.ok().build();
    }

    private boolean esRolAdmin(Usuario usuario) {
        Rol rol = usuario != null ? usuario.getRol() : null;
        return rol == Rol.ADMIN || rol == Rol.ENCARGADO;
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    public record VapidKeyResponse(String publicKey) {
    }

    public record PushSuscripcionRequest(String endpoint, String p256dh, String auth) {
    }
}