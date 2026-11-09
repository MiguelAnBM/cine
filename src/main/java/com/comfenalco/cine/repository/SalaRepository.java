package com.comfenalco.cine.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.dto.response.OcupacionSalaResponse;
import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.enums.EstadoFuncion;
import com.comfenalco.cine.model.enums.EstadoSala;

import jakarta.persistence.LockModeType;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Sala> findByEstadoOrderByNombre(EstadoSala estado);

    // Evita que se programen funciones solapadas en la misma sala al mismo tiempo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sala s WHERE s.id = :id")
    Optional<Sala> findByIdForUpdate(@Param("id") Long id);

    // Ocupación por sala: funciones no canceladas del rango y boletos vigentes vendidos en ellas.
    // Incluye las salas sin funciones (con ceros).
    @Query("""
           SELECT new com.comfenalco.cine.dto.response.OcupacionSalaResponse(
                      s.nombre, COUNT(DISTINCT f.id), COUNT(d.id))
           FROM Sala s
           LEFT JOIN Funcion f ON f.sala = s
                 AND f.fecha BETWEEN :desde AND :hasta
                 AND f.estado <> :cancelada
           LEFT JOIN DetalleBoleto d ON d.funcion = f AND d.asientoActivo IS NOT NULL
           GROUP BY s.id, s.nombre
           ORDER BY s.nombre
           """)
    List<OcupacionSalaResponse> ocupacionPorSala(@Param("desde") LocalDate desde,
                                                 @Param("hasta") LocalDate hasta,
                                                 @Param("cancelada") EstadoFuncion cancelada);
}