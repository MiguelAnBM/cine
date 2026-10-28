package com.comfenalco.cine.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.comfenalco.cine.model.enums.Clasificacion;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.Genero;

@Entity
@Table(name = "peliculas")
public class Pelicula {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pelicula")
    private Long id;

    @NotBlank(message = "El titulo es obligatorio")
    @Column(nullable = false, unique = true, length = 150)
    private String titulo;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sinopsis;

    @NotBlank(message = "La duración es obligatoria")
    @Pattern(regexp = "^\\dH [0-5]?\\dM$", 
            message = "Formato inválido. Las horas deben ser de un solo dígito. Ejemplo: '2H 30M'")
    @Column(nullable = false, length = 6)
    private String duracion;

    @NotNull(message = "La clasificación es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Clasificacion clasificacion;

    @NotNull(message = "El género es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Column(name = "imagen_url", length = 2000)
    private String imagenUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPelicula estado = EstadoPelicula.ACTIVA;

    public Pelicula(){
        
    }

    public Pelicula(String titulo, String sinopsis, String duracion, Clasificacion clasificacion, Genero genero,
            String imagenUrl, EstadoPelicula estado) {
        this.titulo = titulo;
        this.sinopsis = sinopsis;
        this.duracion = duracion;
        this.clasificacion = clasificacion;
        this.genero = genero;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public EstadoPelicula getEstado() {
        return estado;
    }

    public void setEstado(EstadoPelicula estado) {
        this.estado = estado;
    }

    

    
}
