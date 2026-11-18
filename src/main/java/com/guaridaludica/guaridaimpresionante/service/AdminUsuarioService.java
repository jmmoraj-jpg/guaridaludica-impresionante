package com.guaridaludica.guaridaimpresionante.service;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.Direccion;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.repository.DireccionRepository;
import com.guaridaludica.guaridaimpresionante.repository.FavoritoRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final FavoritoRepository favoritoRepository;
    private final DireccionRepository direccionRepository;
    private final ConversacionRepository conversacionRepository;

    public AdminUsuarioService(UsuarioRepository usuarioRepository,
                               FavoritoRepository favoritoRepository,
                               DireccionRepository direccionRepository,
                               ConversacionRepository conversacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.favoritoRepository = favoritoRepository;
        this.direccionRepository = direccionRepository;
        this.conversacionRepository = conversacionRepository;
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void eliminarUsuarioConDependencias(Long usuarioId) {
        // 1. Borrar favoritos del usuario
        favoritoRepository.deleteByUsuarioId(usuarioId);
        
        // 2. Buscar y borrar direcciones del usuario
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario != null) {
            List<Direccion> direcciones = direccionRepository.findByUsuario(usuario);
            if (direcciones != null && !direcciones.isEmpty()) {
                direccionRepository.deleteAll(direcciones);
            }
            
            // 3. Buscar y borrar conversaciones del usuario
            List<Conversacion> conversaciones = conversacionRepository.findByUsuarioId(usuarioId);
            if (conversaciones != null && !conversaciones.isEmpty()) {
                conversacionRepository.deleteAll(conversaciones);
            }
            
            // 4. Forzar flush para ejecutar los deletes en orden
            usuarioRepository.flush();
            
            // 5. Finalmente, borrar el usuario
            usuarioRepository.delete(usuario);
        }
    }
}