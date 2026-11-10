package com.guaridaludica.guaridaimpresionante.util;

import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;

public final class Paginas {

    private Paginas() {}

    // Pide una página (numerada desde 1 para el usuario). Si se pasa de la última, devuelve la última
    public static <T> Page<T> pedir(int paginaUsuario, IntFunction<Page<T>> consulta) {
        int indice = Math.max(paginaUsuario, 1) - 1;
        Page<T> pagina = consulta.apply(indice);
        if (pagina.getTotalPages() > 0 && indice >= pagina.getTotalPages()) {
            pagina = consulta.apply(pagina.getTotalPages() - 1);
        }
        return pagina;
    }

    public static void atributos(Model model, Page<?> page) {
        model.addAttribute("pagina", page.getNumber() + 1);
        model.addAttribute("totalPaginas", page.getTotalPages());
        model.addAttribute("total", page.getTotalElements());
    }

    // Trozo de URL para conservar la búsqueda al cambiar de página
    public static String extraBusqueda(String q) {
        return q == null ? "" : "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
    }
}