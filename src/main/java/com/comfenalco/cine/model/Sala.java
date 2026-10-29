package com.comfenalco.cine.model;

import java.math.BigDecimal;

import org.hibernate.annotations.ColumnDefault;

import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.model.enums.TipoSala;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "salas",
    uniqueConstraints = @UniqueConstraint(name = "uq_sala_nombre", columnNames = "nombre"),
    check = {
        @CheckConstraint(
            name = "chk_sala_precio", 
            constraint = "precio_boleto >= 0"
        )
    }
)
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sala")
    private Long id;

    @NotBlank(message = "El nombre de sala es obligatorio")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotNull(message = "El tipo de sala es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private TipoSala tipo;

    @NotNull(message = "El precio por boleto es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 8 enteros y 2 decimales")
    @Column(name = "precio_boleto", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBoleto;

    @ColumnDefault("'ACTIVA'")
    @NotNull(message = "El estado de la sala es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoSala estado = EstadoSala.ACTIVA;

    protected Sala(){
        
    }
    
    public Sala(String nombre, TipoSala tipo, BigDecimal precioBoleto, EstadoSala estado) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.precioBoleto = precioBoleto;
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

    public TipoSala getTipo() {
        return tipo;
    }

    public void setTipo(TipoSala tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrecioBoleto() {
        return precioBoleto;
    }

    public void setPrecioBoleto(BigDecimal precioBoleto) {
        this.precioBoleto = precioBoleto;
    }

    public EstadoSala getEstado() {
        return estado;
    }

    public void setEstado(EstadoSala estado) {
        this.estado = estado;
    }

    
}
