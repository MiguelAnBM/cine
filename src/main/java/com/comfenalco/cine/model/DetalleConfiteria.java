package com.comfenalco.cine.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "detalles_confiteria",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_conf_factura_producto", 
        columnNames = {"id_factura", "id_producto"}
    ),
    check = {
        @CheckConstraint(
            name = "chk_conf_cantidad", 
            constraint = "cantidad > 0"
        ),
        @CheckConstraint(
            name = "chk_conf_precio",
            constraint = "precio_unitario >= 0"
        )
    }
)
public class DetalleConfiteria {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_confiteria")
    private Long id;

    @NotNull(message = "La factura no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_factura",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_detconf_factura",
            foreignKeyDefinition = 
                "FOREIGN KEY (id_factura) REFERENCES facturas(id_factura) " +
                "ON DELETE RESTRICT ON UPDATE CASCADE"
        )
    )
    private Factura factura;

    @NotNull(message = "El producto no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_producto",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_detconf_producto",
            foreignKeyDefinition = 
                "FOREIGN KEY (id_producto) REFERENCES productos_confiteria(id_producto) " +
                "ON DELETE RESTRICT ON UPDATE CASCADE"
        )
    )
    private ProductoConfiteria productoConfiteria;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos {value}")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo {integer} enteros y {fraction} decimales")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    protected DetalleConfiteria() {

    }

    public DetalleConfiteria(ProductoConfiteria productoConfiteria, Integer cantidad) {
        if (productoConfiteria == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad no puede ser nula");
        }

        this.productoConfiteria = productoConfiteria;
        this.cantidad = cantidad;
        this.precioUnitario = (productoConfiteria == null) ? null : productoConfiteria.getPrecio();
        this.subtotal = calcularSubtotal();
    }

    // Para calcular antes de guardar, cuando "subtotal" todavía es null
    public BigDecimal calcularSubtotal() {

        // Para evitar NullPointerException
        if (precioUnitario == null || cantidad == null) {
            return BigDecimal.ZERO;
        }

        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() {
        return id;
    }

    public Factura getFactura() {
        return factura;
    }

    // Solo Factura (mismo paquete) puede asignar la factura
    void setFactura(Factura factura) {
        this.factura = factura;
    }

    public ProductoConfiteria getProductoConfiteria() {
        return productoConfiteria;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
}
