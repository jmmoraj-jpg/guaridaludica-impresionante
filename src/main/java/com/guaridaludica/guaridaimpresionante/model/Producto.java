package com.guaridaludica.guaridaimpresionante.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC, id ASC")
    private List<ImagenProducto> imagenes = new ArrayList<>();

    private Integer tiempoImpresionMinutos;

    private boolean visible = true;

    @ManyToMany
    @JoinTable(name = "producto_filamento", joinColumns = @JoinColumn(name = "producto_id"), inverseJoinColumns = @JoinColumn(name = "filamento_id"))
    @OrderBy("nombre ASC")
    private Set<Filamento> filamentos = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "producto_categoria", joinColumns = @JoinColumn(name = "producto_id"), inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    @OrderBy("orden ASC, nombre ASC")
    private List<Categoria> categorias = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean novedad;

    public String getTiempoTexto() {
        if (tiempoImpresionMinutos == null)
            return null;
        int h = tiempoImpresionMinutos / 60;
        int m = tiempoImpresionMinutos % 60;
        if (h == 0)
            return m + " min";
        return m == 0 ? h + " h" : h + " h " + m + " min";
    }

    public void addImagen(ImagenProducto imagen) {
        imagen.setProducto(this);
        imagenes.add(imagen);
    }

    public ImagenProducto getImagenPortada() {
        return imagenes.stream()
                .filter(ImagenProducto::isPortada)
                .findFirst()
                .orElse(imagenes.isEmpty() ? null : imagenes.get(0));
    }

    // Imagen para el listado (la plantilla index.html sigue usando p.imagenUrl)
    public String getImagenUrl() {
        ImagenProducto portada = getImagenPortada();
        return portada == null ? null : portada.getUrlMiniatura();
    }

    // Para el carrusel: la portada va la primera
    public List<ImagenProducto> getImagenesCarrusel() {
        List<ImagenProducto> lista = new ArrayList<>(imagenes);
        ImagenProducto portada = getImagenPortada();
        if (portada != null && lista.remove(portada)) {
            lista.add(0, portada);
        }
        return lista;
    }
}