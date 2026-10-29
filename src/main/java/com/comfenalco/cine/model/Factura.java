package com.comfenalco.cine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import com.comfenalco.cine.model.enums.MetodoPago;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity 
@Table(
    name = "facturas",
    indexes = {
        @Index(name = "idx_factura_fecha", columnList = "fecha_hora"),
        @Index(name = "idx_factura_usuario_fecha", columnList = "id_usuario, fecha_hora")
    }
)
public class Factura {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long id;

    @NotNull(message = "El usuario no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_factura_usuario",
            foreignKeyDefinition = 
                "FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) " +
                "ON DELETE RESTRICT ON UPDATE CASCADE"
        )
    )
    private Usuario usuario;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
    
    @NotNull(message = "El método de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 10)
    private MetodoPago metodoPago;

    @Size(max = 100)
    @Column(name = "nombre_cliente", length = 100)
    private String nombreCliente;

    @Email(message = "El correo no tiene un formato válido")   
    @Size(max = 150)
    @Column(name = "correo_cliente", length = 150)
    private String correoCliente;

    @OneToMany(mappedBy = "factura", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<DetalleBoleto> boletos = new ArrayList<>();

    @OneToMany(mappedBy = "factura", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<DetalleConfiteria> productos = new ArrayList<>();
    
    protected Factura() {
        
    }

    public Factura(Usuario usuario, MetodoPago metodoPago, String nombreCliente,
            String correoCliente) {
        this.usuario = usuario;
        this.metodoPago = metodoPago;
        this.nombreCliente = nombreCliente;
        this.correoCliente = correoCliente;
    }

    @PrePersist
    private void asignarFechaHora() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    // Mantiene sincronizados los dos lados de la relación
    public void agregarBoleto(DetalleBoleto boleto) {
        if (boleto == null) {
            throw new IllegalArgumentException("El boleto no puede ser nulo");
        }

        boletos.add(boleto);
        boleto.setFactura(this);
    }

    public void agregarProducto(DetalleConfiteria detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        productos.add(detalle);
        detalle.setFactura(this);
    }

    public List<DetalleConfiteria> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (DetalleBoleto boleto : boletos) {
            BigDecimal precio = boleto.getPrecioUnitario();
            if (precio != null) {
                total = total.add(precio);
            }
        }

        for (DetalleConfiteria detalle : productos) {
            total = total.add(detalle.calcularSubtotal());
        }

        return total;
    }

    // Vista de sólo lectura de boletos para no alterar sincronización
    public List<DetalleBoleto> getBoletos() {
        return Collections.unmodifiableList(boletos);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    
}
