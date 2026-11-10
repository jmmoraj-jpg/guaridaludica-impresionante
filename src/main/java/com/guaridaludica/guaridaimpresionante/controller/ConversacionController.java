package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Conversacion;
import com.guaridaludica.guaridaimpresionante.repository.ConversacionRepository;
import com.guaridaludica.guaridaimpresionante.service.ConversacionService;
import com.guaridaludica.guaridaimpresionante.util.Paginas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/conversaciones")
public class ConversacionController {

    private static final int POR_PAGINA = 10;

    private final ConversacionService servicio;
    private final ConversacionRepository conversacionRepository;

    public ConversacionController(ConversacionService servicio,
                                  ConversacionRepository conversacionRepository) {
        this.servicio = servicio;
        this.conversacionRepository = conversacionRepository;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "1") int pagina,
                        Principal principal, Model model) {
        Page<Conversacion> page = Paginas.pedir(pagina, i ->
                conversacionRepository.findByUsuarioEmailIgnoreCase(principal.getName(),
                        PageRequest.of(i, POR_PAGINA, Sort.by(Sort.Direction.DESC, "ultimaActividad"))));
        model.addAttribute("conversaciones", page.getContent());
        Paginas.atributos(model, page);
        return "conversaciones";
    }

    @PostMapping("/nueva")
    public String nueva(@RequestParam Long productoId, Principal principal) {
        Conversacion c = servicio.iniciar(principal.getName(), productoId);
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
            redirect.addFlashAttribute("error", "Escribe un mensaje (máximo 2000 caracteres).");
        }
        return "redirect:/conversaciones/" + id;
    }
}