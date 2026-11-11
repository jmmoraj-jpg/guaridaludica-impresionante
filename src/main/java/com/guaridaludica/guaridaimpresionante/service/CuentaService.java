package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.EstadoConversacion;
import com.guaridaludica.guaridaimpresionante.model.Rol;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.repository.FavoritoRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CuentaService {

    // Solicitudes que ya están en marcha: no se puede borrar la cuenta con ellas abiertas
    private static final List<EstadoConversacion> EN_CURSO = List.of(
            EstadoConversacion.SOLICITADA,
            EstadoConversacion.ACEPTADA,
            EstadoConversacion.EN_IMPRESION,
            EstadoConversacion.LISTA);

    private final UsuarioRepository usuarioRepository;
    private final FavoritoRepository favoritoRepository;
    private final ConversacionRepository conversacionRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaService(UsuarioRepository usuarioRepository,
            FavoritoRepository favoritoRepository,
            ConversacionRepository conversacionRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.favoritoRepository = favoritoRepository;
        this.conversacionRepository = conversacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void borrarCuenta(String email, String password) {
        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase(email);
        if (encontrado.isEmpty()) {
            throw new IllegalStateException("No se ha encontrado tu cuenta.");
        }
        Usuario usuario = encontrado.get();

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }
        if (usuario.getRol() != Rol.USUARIO) {
            throw new IllegalStateException("Las cuentas del equipo no se pueden borrar desde aquí.");
        }
        if (conversacionRepository.existsByUsuarioIdAndEstadoIn(usuario.getId(), EN_CURSO)) {
            throw new IllegalStateException(
                    "Tienes solicitudes en curso. Cancélalas (si aún se puede) o espera a que se entreguen.");
        }

        favoritoRepository.deleteByUsuarioId(usuario.getId());

        // Los mensajes se borran solos por el cascade de Conversacion
        List<Conversacion> conversaciones = conversacionRepository.findByUsuarioId(usuario.getId());
        for (Conversacion c : conversaciones) {
            conversacionRepository.delete(c);
        }
        conversacionRepository.flush();

        usuarioRepository.delete(usuario);
    }
}