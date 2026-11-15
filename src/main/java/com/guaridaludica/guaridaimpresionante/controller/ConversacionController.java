package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.EstadoConversacion;
import com.guaridaludica.guaridaimpresionante.model.Mensaje;
import com.guaridaludica.guaridaimpresionante.model.Usuario;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.repository.UsuarioRepository;
import com.guaridaludica.guaridaimpresionante.service.CloudinaryService;
import com.guaridaludica.guaridaimpresionante.service.CloudinaryService.ImagenSubida;
import com.guaridaludica.guaridaimpresionante.service.ConversacionService;
import com.guaridaludica.guaridaimpresionante.util.Paginas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/conversaciones")
public class ConversacionController {

    private static final int POR_PAGINA = 10;

    private final ConversacionService servicio;
    private final ConversacionRepository conversacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinaryService;

    public ConversacionController(ConversacionService servicio,
            ConversacionRepository conversacionRepository, UsuarioRepository usuarioRepository,
            CloudinaryService cloudinaryService) {
        this.servicio = servicio;
        this.conversacionRepository = conversacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "1") int pagina,
            Principal principal, Model model) {
        Page<Conversacion> page = Paginas.pedir(pagina,
                i -> conversacionRepository.findByUsuarioEmailIgnoreCase(principal.getName(),
                        PageRequest.of(i, POR_PAGINA, Sort.by(Sort.Direction.DESC, "ultimaActividad"))));
        model.addAttribute("conversaciones", page.getContent());
        Paginas.atributos(model, page);
        return "conversaciones";
    }

    @PostMapping("/nueva")
    public String nueva(@RequestParam Long productoId, Principal principal) {
        Conversacion existente = servicio.buscarAbierta(principal.getName(), productoId);
        if (existente != null) {
            return "redirect:/conversaciones/" + existente.getId();
        }
        return "redirect:/conversaciones/nueva/" + productoId;
    }

    @GetMapping("/nueva/{productoId}")
    public String redactar(@PathVariable Long productoId, Principal principal, Model model) {
        Conversacion existente = servicio.buscarAbierta(principal.getName(), productoId);
        if (existente != null) {
            return "redirect:/conversaciones/" + existente.getId();
        }
        model.addAttribute("conv", servicio.borrador(principal.getName(), productoId));
        model.addAttribute("vistaGuarida", false);
        model.addAttribute("accion", "/conversaciones/nueva/" + productoId + "/mensajes");
        return "conversacion";
    }

    @PostMapping("/nueva/{productoId}/mensajes")
    public String enviarPrimero(@PathVariable Long productoId, @RequestParam String texto,
            Principal principal, RedirectAttributes redirect) {
        Conversacion c = servicio.iniciarConMensaje(principal.getName(), productoId, texto);
        if (c == null) {
            redirect.addFlashAttribute("error", "Escribe un mensaje (máximo 2000 caracteres).");
            return "redirect:/conversaciones/nueva/" + productoId;
        }
        return "redirect:/conversaciones/" + c.getId();
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("conv", servicio.abrirComoUsuario(id, principal.getName()));
        model.addAttribute("vistaGuarida", false);
        model.addAttribute("accion", "/conversaciones/" + id + "/mensajes");
        return "conversacion";
    }

    @PostMapping("/{id}/mensajes")
    public String enviar(@PathVariable Long id, @RequestParam String texto,
            Principal principal, RedirectAttributes redirect) {
        if (!servicio.enviarComoUsuario(id, principal.getName(), texto)) {
            redirect.addFlashAttribute("error",
                    "No se pudo enviar. Comprueba que el mensaje no esté vacío (máximo 2000 caracteres) y que la conversación siga abierta.");
        }
        return "redirect:/conversaciones/" + id;
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam EstadoConversacion estado,
            Principal principal, RedirectAttributes redirect) {
        if (!servicio.cambiarEstadoComoUsuario(id, principal.getName(), estado)) {
            redirect.addFlashAttribute("error", "No se puede hacer ese cambio en este momento.");
        }
        return "redirect:/conversaciones/" + id;
    }

    // Muestra el formulario para proponer una idea personalizada
    @GetMapping("/personalizada")
    public String formularioPersonalizada(Principal principal, Model model) {
        // CORREGIDO: Sin la barra inicial para que Thymeleaf lo encuentre bien
        return "conversaciones/personalizada";
        // O si tu archivo se llama nueva-personalizada.html en la raíz de templates:
        // return "nueva-personalizada";
    }

    // Guarda la propuesta, sube las imágenes y crea la conversación
    @PostMapping("/personalizada/guardar")
    public String guardarConversacionPersonalizada(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam(value = "enlaceReferencia", required = false) String enlaceReferencia,
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
            Principal principal) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Conversacion conv = new Conversacion();
        conv.setUsuario(usuario);
        conv.setEnlaceReferencia(enlaceReferencia);

        // Procesar hasta 3 imágenes con Cloudinary guardando URL y PublicId
        if (imagenes != null) {
            int count = 0;
            for (MultipartFile img : imagenes) {
                if (!img.isEmpty() && count < 3) {
                    try {
                        ImagenSubida resultadoSubida = cloudinaryService.subir(img);
                        String url = resultadoSubida.url();
                        String publicId = resultadoSubida.publicId();

                        if (count == 0) {
                            conv.setImagen1(url);
                            conv.setImagen1PublicId(publicId);
                        } else if (count == 1) {
                            conv.setImagen2(url);
                            conv.setImagen2PublicId(publicId);
                        } else if (count == 2) {
                            conv.setImagen3(url);
                            conv.setImagen3PublicId(publicId);
                        }
                        count++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Creamos el primer mensaje automático con la idea del usuario
        Mensaje primerMensaje = new Mensaje();
        primerMensaje.setAutor(usuario);
        primerMensaje.setTexto("💡 Propuesta personalizada: " + titulo + "\n\n" + descripcion);
        primerMensaje.setDeLaGuarida(false);

        conv.addMensaje(primerMensaje);
        conv.setUltimoResumen("Propuesta personalizada: " + titulo);
        conv.setSinLeerGuarida(true);

        conversacionRepository.save(conv);

        return "redirect:/conversaciones/" + conv.getId();
    }
}