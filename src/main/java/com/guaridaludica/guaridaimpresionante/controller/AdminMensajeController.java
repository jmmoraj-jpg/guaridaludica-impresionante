package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.EstadoConversacion;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.service.CloudinaryService;
import com.guaridaludica.guaridaimpresionante.service.ConversacionService;
import com.guaridaludica.guaridaimpresionante.util.Paginas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/admin/mensajes")
public class AdminMensajeController {

    private static final int POR_PAGINA = 15;

    private final ConversacionService servicio;
    private final ConversacionRepository conversacionRepository;
    private final CloudinaryService cloudinaryService;

    public AdminMensajeController(ConversacionService servicio,
            ConversacionRepository conversacionRepository, CloudinaryService cloudinaryService) {
        this.servicio = servicio;
        this.conversacionRepository = conversacionRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String bandeja(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String filtro,
            Model model) {
        boolean pendientes = "pendientes".equals(filtro);
        boolean canceladas = "canceladas".equals(filtro);

        Page<Conversacion> page = Paginas.pedir(pagina, i -> {
            Pageable pageable = PageRequest.of(i, POR_PAGINA, Sort.by(Sort.Direction.DESC, "ultimaActividad"));
            if (pendientes) {
                return conversacionRepository.findBySinLeerGuaridaTrue(pageable);
            } else if (canceladas) {
                return conversacionRepository.findByEstado(EstadoConversacion.CANCELADA, pageable);
            } else {
                return conversacionRepository.findByUltimoResumenIsNotNull(pageable);
            }
        });

        model.addAttribute("conversaciones", page.getContent());
        Paginas.atributos(model, page);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("canceladas", canceladas);
        model.addAttribute("filtro", filtro);
        
        String extra = "";
        if (pendientes) extra = "&filtro=pendientes";
        else if (canceladas) extra = "&filtro=canceladas";
        model.addAttribute("extraPaginacion", extra);
        
        return "admin/mensajes";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("conv", servicio.abrirComoGuarida(id));
        model.addAttribute("vistaGuarida", true);
        model.addAttribute("accion", "/admin/mensajes/" + id + "/mensajes");
        return "admin/mensaje";
    }

    @PostMapping("/{id}/mensajes")
    public String enviar(@PathVariable Long id, @RequestParam String texto,
            Principal principal, RedirectAttributes redirect) {
        if (!servicio.enviarComoGuarida(id, principal.getName(), texto)) {
            redirect.addFlashAttribute("error",
                "No se pudo enviar. Comprueba que el mensaje no esté vacío (máximo 2000 caracteres) y que la conversación siga abierta.");
        }
        return "redirect:/admin/mensajes/" + id;
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam EstadoConversacion estado,
            @RequestParam(required = false) BigDecimal precio,
            RedirectAttributes redirect) {
        if (!servicio.cambiarEstadoComoGuarida(id, estado, precio)) {
            redirect.addFlashAttribute("error",
                    "No se puede hacer ese cambio. Para aceptar hay que indicar un precio mayor que 0.");
        }
        return "redirect:/admin/mensajes/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarDefinitivamente(@PathVariable Long id, RedirectAttributes redirect) {
        Conversacion conv = conversacionRepository.findById(id).orElse(null);
        if (conv != null && conv.getEstado() == EstadoConversacion.CANCELADA) {
            // Borrar imágenes de Cloudinary si existen
            if (conv.getImagen1PublicId() != null) {
                cloudinaryService.borrar(conv.getImagen1PublicId());
            }
            if (conv.getImagen2PublicId() != null) {
                cloudinaryService.borrar(conv.getImagen2PublicId());
            }
            if (conv.getImagen3PublicId() != null) {
                cloudinaryService.borrar(conv.getImagen3PublicId());
            }

            conversacionRepository.delete(conv);
            redirect.addFlashAttribute("error", "Conversación y sus imágenes eliminadas definitivamente.");
        }
        return "redirect:/admin/mensajes?filtro=canceladas";
    }

    @PostMapping("/{id}/detalle")
    public String guardarDetalle(@PathVariable Long id,
            @RequestParam(required = false) String resumenEncargo,
            RedirectAttributes redirect) {
        if (!servicio.actualizarResumenEncargoComoGuarida(id, resumenEncargo)) {
            redirect.addFlashAttribute("error",
                    "Solo puedes indicar o modificar el detalle de piezas y colores mientras la conversación esté en Consulta.");
        }
        return "redirect:/admin/mensajes/" + id;
    }
}