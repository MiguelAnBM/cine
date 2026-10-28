package com.comfenalco.cine.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.comfenalco.cine.model.enums.EstadoFuncion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity 
@Table (name = "funciones")
public class Funcion {
 @Id 
 @GeneratedValue(strategy= GenerationType.IDENTITY )
 @Column (name = "id_funcion")
private Long id;
//EDITAR POR RELACIONES
@Column (name = "id_pelicula", nullable = false)
@NotNull (message = "El identificador de la pelicula es obligatorio")
private Long idPelicula;

@Column (name = "id_sala", nullable = false)
@NotNull (message = "El identificador de la sala es obligatorio")
private Long idSala;

@Column (nullable = false)
@NotNull (message = "La fecha es obligatoria")
private LocalDate fecha;

@Column (name = "hora_inicio", nullable = false)
@NotNull (message = "La hora de inicio es obligatoria")
private LocalTime horaInicio;

@Column (nullable = false)
@Enumerated(EnumType.STRING)
@NotNull (message = "El estado es obligatorio")
private EstadoFuncion estado = EstadoFuncion.PROGRAMADA;

public Funcion(Long idPelicula, Long idSala, LocalDate fecha, LocalTime horaInicio, EstadoFuncion estado) {
    this.idPelicula = idPelicula;
    this.idSala = idSala;
    this.fecha = fecha;
    this.horaInicio = horaInicio;
    this.estado = estado;
}
public Funcion(){}

public Long getId() {
    return id;
}
public void setId(Long id) {
    this.id = id;
}
public Long getIdPelicula() {
    return idPelicula;
} 
public void setIdPelicula(Long idPelicula) {
    this.idPelicula = idPelicula;
}
public Long getIdSala() {
    return idSala;
}
public void setIdSala(Long idSala) {
    this.idSala = idSala;
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

