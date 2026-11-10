package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.model.*;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversacionService {

    private static final int MAX_TEXTO = 2000;
    private static final List<EstadoConversacion> CERRADAS =
            List.of(EstadoConversacion.ENTREGADA, EstadoConversacion.CANCELADA);

    private final ConversacionRepository conversacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public ConversacionService(ConversacionRepository conversacionRepository,
                               UsuarioRepository usuarioRepository,
                               ProductoRepository productoRepository) {
        this.conversacionRepository = conversacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    // Retoma la conversación abierta sobre esa pieza o crea una nueva
    @Transactional
    public Conversacion iniciar(String email, Long productoId) {
        List<Conversacion> abiertas = conversacionRepository.abiertasDe(email, productoId, CERRADAS);
        if (!abiertas.isEmpty()) {
            return abiertas.get(0);
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        Producto producto = productoRepository.findById(productoId)
                .filter(Producto::isVisible)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Conversacion c = new Conversacion();
        c.setUsuario(usuario);
        c.setProducto(producto);
        return conversacionRepository.save(c);
    }

    // Solo el dueño de la conversación puede abrirla desde la parte de usuario
    @Transactional
    public Conversacion abrirComoUsuario(Long id, String email) {
        Conversacion c = conversacionRepository.findById(id)
                .filter(x -> x.getUsuario().getEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setSinLeerUsuario(false);
        return c;
    }

    @Transactional
    public Conversacion abrirComoGuarida(Long id) {
        Conversacion c = conversacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setSinLeerGuarida(false);
        return c;
    }

    @Transactional
    public boolean enviarComoUsuario(Long id, String email, String texto) {
        return anadir(abrirComoUsuario(id, email), email, texto, false);
    }

    @Transactional
    public boolean enviarComoGuarida(Long id, String email, String texto) {
        return anadir(abrirComoGuarida(id), email, texto, true);
    }

    private boolean anadir(Conversacion c, String email, String texto, boolean deLaGuarida) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty() || limpio.length() > MAX_TEXTO) {
            return false;
        }
        Usuario autor = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();

        Mensaje m = new Mensaje();
        m.setAutor(autor);
        m.setTexto(limpio);
        m.setDeLaGuarida(deLaGuarida);
        c.addMensaje(m);

        c.setUltimaActividad(LocalDateTime.now());
        c.setUltimoResumen(limpio.length() > 120 ? limpio.substring(0, 117) + "…" : limpio);
        if (deLaGuarida) {
            c.setSinLeerUsuario(true);
        } else {
            c.setSinLeerGuarida(true);
        }
        return true;
    }
}