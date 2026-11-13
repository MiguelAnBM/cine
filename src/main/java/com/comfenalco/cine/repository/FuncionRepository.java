package com.comfenalco.cine.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Funcion f WHERE f.id = :id")
    Optional<Funcion> findByIdForUpdate(@Param("id") Long id);

    // Funciones de una sala entre dos fechas, con su película (para calcular la duración)
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            WHERE f.sala.id = :idSala
            AND f.fecha BETWEEN :desde AND :hasta
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findDeSalaEnRango(@Param("idSala") Long idSala,
                                    @Param("desde") LocalDate desde,
                                    @Param("hasta") LocalDate hasta);

    // Funciones del rango con su película para calcular la duración (reemplaza findNoCanceladasEnRango)
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            WHERE f.fecha BETWEEN :desde AND :hasta
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findEnRango(@Param("desde") LocalDate desde,
                              @Param("hasta") LocalDate hasta);

    // Funciones de una sala desde hoy en adelante, con pelicula cargada (para cambiarEstado)
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            WHERE f.sala.id = :idSala AND f.fecha >= :desde
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findBySalaIdAndFechaGreaterThanEqual(@Param("idSala") Long idSala,
                                                       @Param("desde") LocalDate desde);

    // Funciones de una pelicula desde hoy en adelante, con pelicula cargada (para cambiarEstado)
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            WHERE f.pelicula.id = :idPelicula AND f.fecha >= :desde
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findByPeliculaIdAndFechaGreaterThanEqual(@Param("idPelicula") Long idPelicula,
                                                           @Param("desde") LocalDate desde);

    // Listado de todas las funciones (pantalla Funciones): película y sala sin consultas extra
    @EntityGraph(attributePaths = {"pelicula", "sala"})
    Page<Funcion> findAll(Pageable pageable);

    // Próximas funciones: las que aún no empiezan, de la más cercana a la más lejana
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            JOIN FETCH f.sala
            WHERE (f.fecha > :hoy OR (f.fecha = :hoy AND f.horaInicio >= :ahora))
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findProximas(@Param("hoy") LocalDate hoy,
                               @Param("ahora") LocalTime ahora,
                               Pageable pageable);

    // Próximas funciones del día actual (pantalla Inicio): solo hoy y que aún no comiencen
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            JOIN FETCH f.sala
            WHERE f.fecha = :hoy
            AND f.horaInicio >= :ahora
            ORDER BY f.horaInicio
        """)
    List<Funcion> findProximasDeHoy(@Param("hoy") LocalDate hoy,
                                    @Param("ahora") LocalTime ahora,
                                    Pageable pageable);

    // Cuenta funciones de una fecha
    @Query("SELECT COUNT(f) FROM Funcion f WHERE f.fecha = :fecha")
    long countByFecha(@Param("fecha") LocalDate fecha);
}
