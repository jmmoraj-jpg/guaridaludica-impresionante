package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.model.EstadoConversacion;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
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

    public AdminMensajeController(ConversacionService servicio,
            ConversacionRepository conversacionRepository) {
        this.servicio = servicio;
        this.conversacionRepository = conversacionRepository;
    }

    @GetMapping
    public String bandeja(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String filtro,
            Model model) {
        boolean pendientes = "pendientes".equals(filtro);

        Page<Conversacion> page = Paginas.pedir(pagina, i -> {
            Pageable pageable = PageRequest.of(i, POR_PAGINA, Sort.by(Sort.Direction.DESC, "ultimaActividad"));
            return pendientes
                    ? conversacionRepository.findBySinLeerGuaridaTrue(pageable)
                    : conversacionRepository.findByUltimoResumenIsNotNull(pageable);
        });

        model.addAttribute("conversaciones", page.getContent());
        Paginas.atributos(model, page);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("extraPaginacion", pendientes ? "&filtro=pendientes" : "");
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
}