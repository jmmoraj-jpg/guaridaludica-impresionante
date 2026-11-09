package com.guaridaludica.guaridaimpresionante.config;

import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.model.Rol;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInicializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInicializer(UsuarioRepository usuarioRepository,
                           ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder,
                           Environment env) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @Override
    public void run(String... args) {
        crearAdmin();
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            crearPiezasDeEjemplo();
        }
    }

    private void crearAdmin() {
        if (usuarioRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail(adminEmail.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol(Rol.ADMIN);
        usuarioRepository.save(admin);
    }

    // Solo en dev y solo si la tabla está vacía
    private void crearPiezasDeEjemplo() {
        if (productoRepository.count() > 0) {
            return;
        }
        productoRepository.save(nuevaPieza("Dragón articulado",
                "Dragón flexible que se mueve entero, impreso en una sola pieza.",
                "Figuras", "PLA", 360));
        productoRepository.save(nuevaPieza("Soporte para mando",
                "Soporte sencillo para dejar el mando de la consola a mano.",
                "Decoración", "PETG", 180));
    }

    private Producto nuevaPieza(String nombre, String descripcion,
                                String categoria, String material, int minutos) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setCategoria(categoria);
        p.setMaterial(material);
        p.setTiempoImpresionMinutos(minutos);
        return p;
    }
}