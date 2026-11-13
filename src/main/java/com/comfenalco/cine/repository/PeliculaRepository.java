package com.comfenalco.cine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.enums.EstadoPelicula;

import jakarta.persistence.LockModeType;

public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pelicula p WHERE p.id = :id")
    Optional<Pelicula> findByIdForUpdate(@Param("id") Long id);

    List<Pelicula> findByEstado(EstadoPelicula estado);

    boolean existsByTituloIgnoreCase(String titulo);
    
    boolean existsByTituloIgnoreCaseAndIdNot(String titulo, Long id);
}
