package com.comfenalco.cine.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Evita que una función se venda mientras otro usuario la cancela
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Funcion f WHERE f.id = :id")
    Optional<Funcion> findByIdForUpdate(@Param("id") Long id);

    // Busca Películas con funciones programadas después de {fecha}
    boolean existsByPeliculaIdAndEstadoAndFechaGreaterThanEqual(
        Long idPelicula, EstadoFuncion estado, LocalDate fecha);

    // Busca Salas con funciones programadas después de {fecha}
    boolean existsBySalaIdAndEstadoAndFechaGreaterThanEqual(
        Long idSala, EstadoFuncion estado, LocalDate fecha);

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

    // Funciones de una sala entre dos fechas, con su película (para calcular la duración)
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            WHERE f.sala.id = :idSala
            AND f.fecha BETWEEN :desde AND :hasta
            AND f.estado <> :excluido
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findDeSalaEnRango(@Param("idSala") Long idSala,
                                    @Param("desde") LocalDate desde,
                                    @Param("hasta") LocalDate hasta,
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

    // Listado de todas las funciones (pantalla Funciones): película y sala sin consultas extra
    @EntityGraph(attributePaths = {"pelicula", "sala"})
    Page<Funcion> findAll(Pageable pageable);

    // Próximas funciones (pantalla Inicio): las que aún no empiezan, de la más cercana a la más lejana
    @Query(
        """
            SELECT f FROM Funcion f
            JOIN FETCH f.pelicula
            JOIN FETCH f.sala
            WHERE f.estado = :estado
            AND (f.fecha > :hoy OR (f.fecha = :hoy AND f.horaInicio >= :ahora))
            ORDER BY f.fecha, f.horaInicio
        """)
    List<Funcion> findProximas(@Param("estado") EstadoFuncion estado,
                               @Param("hoy") LocalDate hoy,
                               @Param("ahora") LocalTime ahora,
                               Pageable pageable);

    // Funciones en un estado dentro de un rango de fechas (ambos extremos incluidos)
    long countByEstadoAndFechaBetween(EstadoFuncion estado, LocalDate desde, LocalDate hasta);
}