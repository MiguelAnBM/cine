package com.comfenalco.cine.model;

import java.math.BigDecimal;

import org.hibernate.annotations.ColumnDefault;

import com.comfenalco.cine.model.enums.EstadoProducto;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "productos_confiteria",
    uniqueConstraints = @UniqueConstraint(name = "uq_producto_nombre", columnNames = "nombre"),
    check = {
        @CheckConstraint(
            name = "chk_producto_precio", 
            constraint = "precio >= 0"
        )
    }
)
public class ProductoConfiteria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 8 enteros y 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Size(max = 500)
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @ColumnDefault("'ACTIVO'")
    @NotNull(message = "El estado del producto es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoProducto estado = EstadoProducto.ACTIVO;
    
    protected ProductoConfiteria(){
        
    }

    public ProductoConfiteria(String nombre, String descripcion, BigDecimal precio, String imagenUrl, EstadoProducto estado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
    }

    
}
