package com.guaridaludica.guaridaimpresionante.config;

import com.guaridaludica.guaridaimpresionante.model.Filamento;
import com.guaridaludica.guaridaimpresionante.model.Producto;
import com.guaridaludica.guaridaimpresionante.model.Rol;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.FilamentoRepository;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import com.guaridaludica.guaridaimpresionante.service.CategoriaService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInicializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final FilamentoRepository filamentoRepository;
    private final CategoriaService categoriaService;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInicializer(UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            FilamentoRepository repository,
            CategoriaService categoriaService,
            PasswordEncoder passwordEncoder,
            Environment env) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.filamentoRepository = repository;
        this.categoriaService = categoriaService;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @Override
    @Transactional
    public void run(String... args) {
        crearAdmin();
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            crearPiezasDeEjemplo();
            crearFilamentosDeEjemplo();
        }
    }

    private void crearFilamentosDeEjemplo() {
        if (filamentoRepository.count() > 0) {
            return;
        }
       
        filamentoRepository.save(nuevoFilamento("PLA Negro Mate", "#212529")); 
        filamentoRepository.save(nuevoFilamento("PLA Seda Oro", "#E6C687"));
        filamentoRepository.save(nuevoFilamento("PLA Verde Menta", "#4EBA93"));
        
    };
    
    private Filamento nuevoFilamento(String nombre, String colorHex) {
        Filamento filamento = new Filamento();
        filamento.setNombre(nombre);
        filamento.setColorHex(colorHex);
        return filamento;
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
                "PLA", 360, "Figuras", "Regalos"));
        productoRepository.save(nuevaPieza("Soporte para mando",
                "Soporte sencillo para dejar el mando de la consola a mano.",
                "PETG", 180, "Decoración"));
    }

    private Producto nuevaPieza(String nombre, String descripcion, String material,
            int minutos, String... categorias) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setMaterial(material);
        p.setTiempoImpresionMinutos(minutos);
        for (String c : categorias) {
            p.getCategorias().add(categoriaService.obtenerOCrear(c));
        }
        return p;
    }
}