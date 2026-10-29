package com.comfenalco.cine.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.ColumnDefault;

import com.comfenalco.cine.model.enums.EstadoFuncion;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity 
@Table (
    name = "funciones",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_funcion_sala_fecha_hora", 
        columnNames = {"id_sala", "fecha", "hora_inicio"}
    ),
    indexes = {
        @Index(name = "idx_funcion_fecha_hora", columnList = "fecha, hora_inicio"),
        @Index(name = "idx_funcion_pelicula_fecha", columnList = "id_pelicula, fecha")
    }
)
public class Funcion {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    @Column(name = "id_funcion")
    private Long id;

    @NotNull(message = "La película no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_pelicula",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_funcion_pelicula",
            foreignKeyDefinition = "FOREIGN KEY (id_pelicula) REFERENCES peliculas(id_pelicula) " +
                                   "ON DELETE RESTRICT ON UPDATE CASCADE"
        )
    )
    private Pelicula pelicula;

    @NotNull(message = "La sala no puede ser nula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_sala",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_funcion_sala",
            foreignKeyDefinition = "FOREIGN KEY (id_sala) REFERENCES salas(id_sala) " +
                                   "ON DELETE RESTRICT ON UPDATE CASCADE"
        )
    )
    private Sala sala;
    
    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;
    
    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @ColumnDefault("'PROGRAMADA'")
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoFuncion estado = EstadoFuncion.PROGRAMADA;

    protected Funcion(){

    }

    public Funcion(Pelicula pelicula, Sala sala, LocalDate fecha, LocalTime horaInicio, EstadoFuncion estado) {
        this.pelicula = pelicula;
        this.sala = sala;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Pelicula getPelicula() {
        return pelicula;
    } 

    public void setPelicula(Pelicula pelicula) {
        this.pelicula = pelicula;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public EstadoFuncion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoFuncion estado) {
        this.estado = estado;
    }
}

