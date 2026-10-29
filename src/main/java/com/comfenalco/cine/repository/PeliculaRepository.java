package com.comfenalco.cine.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.Genero;

public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {
    
    List<Pelicula> findByEstado(EstadoPelicula estado);

    List<Pelicula> findByGeneroAndEstado(Genero genero, EstadoPelicula estado);

    Page<Pelicula> findByTituloContainingIgnoreCaseAndEstado(
            String titulo, EstadoPelicula estado, Pageable pageable);   // búsqueda paginada
}
