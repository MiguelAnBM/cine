package com.comfenalco.cine.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.model.Funcion;
import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.enums.EstadoFuncion;
import com.comfenalco.cine.model.enums.EstadoPelicula;

import jakarta.persistence.LockModeType;

public interface FuncionRepository extends JpaRepository<Funcion, Long> {

    // Evita que una función se venda mientras otro usuario la cancela
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Funcion f WHERE f.id = :id")
    Optional<Funcion> findByIdForUpdate(@Param("id") Long id);
    
    boolean existsBySalaIdAndFechaAndHoraInicio(Long idSala, LocalDate fecha, LocalTime horaInicio);

    boolean existsByPeliculaId(Long idPelicula);   // para no desactivar/borrar una película con funciones
    
    boolean existsBySalaId(Long idSala);

    // Cartelera del día: película y sala en una sola consulta
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            JOIN FETCH f.sala
            WHERE f.fecha = :fecha AND f.estado = :estado
            ORDER BY f.horaInicio
        """)
    List<Funcion> findCartelera(@Param("fecha") LocalDate fecha,
                                @Param("estado") EstadoFuncion estado);

    // Para validar solapamientos: funciones de una sala ese día, con su película (duración)
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            WHERE f.sala.id = :idSala AND f.fecha = :fecha AND f.estado <> :excluido
            ORDER BY f.horaInicio
        """)
    List<Funcion> findDelDiaPorSala(@Param("idSala") Long idSala,
                                    @Param("fecha") LocalDate fecha,
                                    @Param("excluido") EstadoFuncion excluido);

    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.sala
            WHERE f.pelicula.id = :idPelicula AND f.fecha >= :desde AND f.estado = :estado
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findProximasPorPelicula(@Param("idPelicula") Long idPelicula,
                                        @Param("desde") LocalDate desde,
                                        @Param("estado") EstadoFuncion estado);

    @Query(
        """
            SELECT DISTINCT f.pelicula FROM Funcion f
            WHERE f.fecha >= :desde AND f.estado = :estadoFuncion
            AND f.pelicula.estado = :estadoPelicula
        """)
    List<Pelicula> findPeliculasEnCartelera(@Param("desde") LocalDate desde,
                                            @Param("estadoFuncion") EstadoFuncion estadoFuncion,
                                            @Param("estadoPelicula") EstadoPelicula estadoPelicula);
}

