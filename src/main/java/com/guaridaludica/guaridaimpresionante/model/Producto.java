package com.guaridaludica.guaridaimpresionante.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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

    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double altoCm;
    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double anchoCm;
    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double fondoCm;

    private boolean visible = true;

    // Datos económicos calculados y guardados
    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double costeProduccion;

    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double margenBeneficioAplicado;

    @DecimalMin(value = "0.0", message = "No puede ser negativo")
    private Double precioSugerido;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoFilamento> filamentosUsados = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "producto_categoria", joinColumns = @JoinColumn(name = "producto_id"), inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    @OrderBy("orden ASC, nombre ASC")
    private List<Categoria> categorias = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean novedad;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer stock = 0;

    public String getTiempoTexto() {
        if (tiempoImpresionMinutos == null)
            return null;
        int h = tiempoImpresionMinutos / 60;
        int m = tiempoImpresionMinutos % 60;
        if (h == 0)
            return m + " min";
        return m == 0 ? h + " h" : h + " h " + m + " min";
    }

    public String getDimensionesTexto() {
        if (altoCm == null || anchoCm == null || fondoCm == null) {
            return null;
        }
        return formatearMedida(altoCm) + " × " + formatearMedida(anchoCm) + " × " + formatearMedida(fondoCm) + " cm";
    }

    private String formatearMedida(Double v) {
        return (v % 1 == 0) ? String.valueOf(v.intValue()) : String.valueOf(v);
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

    public List<Filamento> getFilamentosDisponibles() {
        return filamentosUsados.stream().map(ProductoFilamento::getFilamento).toList();
    }
}