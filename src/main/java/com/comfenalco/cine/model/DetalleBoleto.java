package com.comfenalco.cine.model;

import java.math.BigDecimal;
import java.util.Locale;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity 
@Table(
    name = "detalles_boleto",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_boleto_funcion_asiento", 
        columnNames = {"id_funcion", "codigo_asiento"}
    ),
    check = {
        @CheckConstraint(
            name = "chk_boleto_precio", 
            constraint = "precio_unitario >= 0"
        ),
        @CheckConstraint(
            name = "chk_boleto_asiento",
            constraint = "REGEXP_LIKE(codigo_asiento, '^[A-H]([1-9]|10)$', 'c')"
        )
    }
)
public class DetalleBoleto {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_boleto")
    private Long id;

    @NotNull(message = "La factura no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_factura",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_detboleto_factura",
            foreignKeyDefinition = "FOREIGN KEY (id_factura) REFERENCES facturas(id_factura) " +
                                   "ON DELETE RESTRICT ON UPDATE CASCADE"
        )
    )
    private Factura factura;

    @NotNull(message = "La función no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_funcion",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_detboleto_funcion",
            foreignKeyDefinition = "FOREIGN KEY (id_funcion) REFERENCES funciones(id_funcion) " +
                                   "ON DELETE RESTRICT ON UPDATE CASCADE"
        )
    )
    private Funcion funcion;

    @NotBlank(message = "El asiento es obligatorio")
    @Pattern(
        regexp = "^[A-H]([1-9]|10)$",
        message = "El asiento debe ser una letra de la A a la H seguida de un número del 1 al 10 (ej: A1, H10)"
    )
    @Column(name = "codigo_asiento", nullable = false, length = 3)
    private String codigoAsiento;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo {integer} enteros y {fraction} decimales")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    
    protected DetalleBoleto() {

    }

    public DetalleBoleto(Funcion funcion, String codigoAsiento) {
        this.funcion = funcion;
        this.codigoAsiento = (codigoAsiento == null) ? null : codigoAsiento.trim().toUpperCase(Locale.ROOT);
        this.precioUnitario = funcion.getSala().getPrecioBoleto();   // copia del precio vigente
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

    public Funcion getFuncion() {
        return funcion;
    }

    public String getCodigoAsiento() {
        return codigoAsiento;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
}
