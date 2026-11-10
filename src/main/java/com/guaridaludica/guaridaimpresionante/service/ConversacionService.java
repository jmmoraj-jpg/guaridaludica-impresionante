package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.model.*;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversacionService {

    private static final int MAX_TEXTO = 2000;
    private static final List<EstadoConversacion> CERRADAS = List.of(EstadoConversacion.ENTREGADA,
            EstadoConversacion.CANCELADA);

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

    // Devuelve la conversación abierta del usuario sobre esa pieza, o null si no
    // hay
    @Transactional(readOnly = true)
    public Conversacion buscarAbierta(String email, Long productoId) {
        List<Conversacion> abiertas = conversacionRepository.abiertasDe(email, productoId, CERRADAS);
        if (abiertas.isEmpty()) {
            return null;
        }
        return abiertas.get(0);
    }

    // Conversación en memoria, NO se guarda: solo sirve para pintar la pantalla de
    // redacción
    @Transactional(readOnly = true)
    public Conversacion borrador(String email, Long productoId) {
        return nuevaConversacion(email, productoId);
    }

    // Crea la conversación y su primer mensaje a la vez. Devuelve null si el texto
    // no es válido
    @Transactional
    public Conversacion iniciarConMensaje(String email, Long productoId, String texto) {
        Conversacion c = buscarAbierta(email, productoId);
        if (c == null) {
            c = nuevaConversacion(email, productoId);
        }
        if (!anadir(c, email, texto, false)) {
            return null;
        }
        return conversacionRepository.save(c);
    }

    private Conversacion nuevaConversacion(String email, Long productoId) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null || !producto.isVisible()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Conversacion c = new Conversacion();
        c.setUsuario(usuario);
        c.setProducto(producto);
        return c;
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
        if (c.getEstado().isCerrada()) {
            return false;
        }
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

    @Transactional
    public boolean cambiarEstadoComoUsuario(Long id, String email, EstadoConversacion nuevo) {
        Conversacion c = abrirComoUsuario(id, email);
        if (!c.getEstado().usuarioPuedePasarA(nuevo)) {
            return false;
        }
        c.setEstado(nuevo);
        c.setUltimaActividad(LocalDateTime.now());
        c.setSinLeerGuarida(true);
        return true;
    }

    @Transactional
    public boolean cambiarEstadoComoGuarida(Long id, EstadoConversacion nuevo, BigDecimal precio) {
        Conversacion c = abrirComoGuarida(id);
        if (nuevo == EstadoConversacion.SOLICITADA || !c.getEstado().puedePasarA(nuevo)) {
            return false;
        }
        if (nuevo == EstadoConversacion.ACEPTADA) {
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            c.setPrecio(precio);
        }
        c.setEstado(nuevo);
        c.setUltimaActividad(LocalDateTime.now());
        c.setSinLeerUsuario(true);
        return true;
    }
}