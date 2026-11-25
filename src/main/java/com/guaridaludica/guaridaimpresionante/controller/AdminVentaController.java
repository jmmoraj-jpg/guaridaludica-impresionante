package com.guaridaludica.guaridaimpresionante.controller;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import com.guaridaludica.guaridaimpresionante.model.Venta;
import com.guaridaludica.guaridaimpresionante.repository.ProductoRepository;
import com.guaridaludica.guaridaimpresionante.repository.VentaRepository;
import com.guaridaludica.guaridaimpresionante.service.VentaService;
import com.guaridaludica.guaridaimpresionante.util.Paginas;

@Controller
@RequestMapping("/admin/ventas")
public class AdminVentaController {

    private static final int POR_PAGINA = 15;

    private final VentaRepository ventaRepository;
    private final VentaService ventaService;
    private final ProductoRepository productoRepository;

    public AdminVentaController(VentaRepository ventaRepository,
            VentaService ventaService,
            ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.ventaService = ventaService;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public String verVentas(@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String origen,
            Model model) {
        String busqueda = (q == null || q.isBlank()) ? null : q.trim();
        String patron = (busqueda == null) ? null : "%" + busqueda.toLowerCase() + "%";
        String origenFiltro = (origen == null || origen.isBlank() || "TODAS".equalsIgnoreCase(origen))
                ? null
                : origen.trim().toUpperCase();

        Page<Venta> page = Paginas.pedir(pagina, i -> {
            Pageable pageable = PageRequest.of(i, POR_PAGINA,
                    Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id")));
            return ventaRepository.buscar(patron, origenFiltro, pageable);
        });

        BigDecimal totalIngresos = ventaRepository.sumarIngresosTotales();
        BigDecimal totalCostes = ventaRepository.sumarCostesTotales();
        BigDecimal beneficioEstimado = totalIngresos.subtract(totalCostes);
        Long totalUnidades = ventaRepository.sumarUnidadesTotales();

        StringBuilder extra = new StringBuilder();
        if (busqueda != null) {
            extra.append("&q=").append(UriUtils.encodeQueryParam(busqueda, StandardCharsets.UTF_8));
        }
        if (origenFiltro != null) {
            extra.append("&origen=").append(UriUtils.encodeQueryParam(origenFiltro, StandardCharsets.UTF_8));
        }

        model.addAttribute("ventas", page.getContent());
        Paginas.atributos(model, page);
        model.addAttribute("q", busqueda);
        model.addAttribute("origen", origenFiltro != null ? origenFiltro : "TODAS");
        model.addAttribute("extraPaginacion", extra.toString());

        model.addAttribute("productos", productoRepository.findAll(Sort.by("nombre").ascending()));
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("totalCostes", totalCostes);
        model.addAttribute("beneficioEstimado", beneficioEstimado);
        model.addAttribute("totalUnidades", totalUnidades != null ? totalUnidades : 0);
        return "admin/ventas";
    }

    @PostMapping("/registrar")
    public String registrar(@RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String concepto,
            @RequestParam(defaultValue = "1") int unidades,
            @RequestParam BigDecimal precioTotal,
            @RequestParam(defaultValue = "true") boolean descontarStock,
            RedirectAttributes redirect) {
        if (precioTotal == null || precioTotal.compareTo(BigDecimal.ZERO) < 0) {
            redirect.addFlashAttribute("error", "Indica un importe válido.");
            return "redirect:/admin/ventas";
        }
        ventaService.registrarVentaManual(productoId, concepto, unidades, precioTotal, descontarStock);
        redirect.addFlashAttribute("mensaje", "Venta registrada correctamente.");
        return "redirect:/admin/ventas";
    }
}