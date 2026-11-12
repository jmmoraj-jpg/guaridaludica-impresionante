package com.guaridaludica.guaridaimpresionante.controller;

import com.guaridaludica.guaridaimpresionante.model.Filamento;
import com.guaridaludica.guaridaimpresionante.repository.FilamentoRepository;
import com.guaridaludica.guaridaimpresionante.service.FilamentoService;
import com.guaridaludica.guaridaimpresionante.util.Paginas;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/filamentos")
public class AdminFilamentoController {
    private static final int POR_PAGINA = 15;

    private final FilamentoRepository filamentoRepository;
    private final FilamentoService filamentoService;

    public AdminFilamentoController(FilamentoRepository filamentoRepository,
            FilamentoService filamentoService) {
        this.filamentoRepository = filamentoRepository;
        this.filamentoService = filamentoService;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String q,
            Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new FilamentoForm());
        }
        cargarListado(model, pagina, q);
        return "admin/filamentos";
    }

    private void cargarListado(Model model, int pagina, String q) {
        String busqueda = (q == null || q.isBlank()) ? null : q.trim();

        Page<Filamento> page = Paginas.pedir(pagina, i -> {
            Pageable pageable = PageRequest.of(i, POR_PAGINA,
                    Sort.by("nombre").ascending());
            return busqueda == null
                    ? filamentoRepository.findAll(pageable)
                    : filamentoRepository.findByNombreContainingIgnoreCase(busqueda, pageable);
        });

        model.addAttribute("filamentos", page.getContent());
        Paginas.atributos(model, page);
        model.addAttribute("q", busqueda);
        model.addAttribute("extraPaginacion", Paginas.extraBusqueda(busqueda));
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String q,
            Model model) {

        Filamento f = filamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Inyectamos el formulario con los datos del filamento a editar
        model.addAttribute("form", FilamentoForm.desde(f));

        // Cargamos la lista y la paginación normalmente para que la tabla inferior no
        // falle
        cargarListado(model, pagina, q);

        return "admin/filamentos";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("form") FilamentoForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (!result.hasFieldErrors("nombre")
                && filamentoService.nombreEnUso(form.getNombre(), form.getId())) {
            result.rejectValue("nombre", "duplicado", "Ya existe un filamento con ese nombre");
        }

        if (result.hasErrors()) {
            cargarListado(model, 1, null);
            return "admin/filamentos";
        }

        filamentoService.guardar(form);
        redirect.addFlashAttribute("mensaje", "Filamento guardado correctamente");
        return "redirect:/admin/filamentos";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id) {
        filamentoService.cambiarEstado(id);
        return "redirect:/admin/filamentos";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id, RedirectAttributes redirect) {
        filamentoService.borrar(id);
        redirect.addFlashAttribute("mensaje", "Filamento borrado");
        return "redirect:/admin/filamentos";
    }
}