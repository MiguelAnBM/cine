package com.comfenalco.cine.dto.response;

import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.enums.Clasificacion;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.Genero;

public record PeliculaResponse(
    Long id, String titulo, String sinopsis, String duracion,
    Clasificacion clasificacion, Genero genero, String imagenUrl, EstadoPelicula estado
) {
    public static PeliculaResponse de(Pelicula p) {
        return new PeliculaResponse(
                p.getId(), p.getTitulo(), p.getSinopsis(), p.getDuracion(),
                p.getClasificacion(), p.getGenero(), p.getImagenUrl(), p.getEstado());
    }
}
