package com.comfenalco.cine.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;

import com.comfenalco.cine.model.enums.Clasificacion;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.Genero;

@Entity
@Table(
    name = "peliculas",
    check = { 
        @CheckConstraint(
            name = "chk_pelicula_duracion",
            constraint = 
                "REGEXP_LIKE(duracion, '^[0-9]H [0-5]?[0-9]M$', 'c') " +
                "AND NOT REGEXP_LIKE(duracion, '^0H 0?0M$', 'c')"
        )
    }
)
public class Pelicula {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pelicula")
    private Long id;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sinopsis;

    // Formato "2H 30M", y no puede ser 0H 0M
    @NotBlank(message = "La duración es obligatoria")
    @Pattern(
        regexp = "^(?!0H 0?0M$)[0-9]H [0-5]?[0-9]M$", 
        message = "La duración debe tener el formato '2H 30M' y ser mayor a cero")
    @Column(nullable = false, length = 6)
    private String duracion;

    @NotNull(message = "La clasificación es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Clasificacion clasificacion;

    @NotNull(message = "El género es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero;

    @Size(max = 2000)
    @Column(name = "imagen_url", length = 2000)
    private String imagenUrl;
    
    @ColumnDefault("'ACTIVA'")
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoPelicula estado = EstadoPelicula.ACTIVA;

    protected Pelicula(){
        
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
