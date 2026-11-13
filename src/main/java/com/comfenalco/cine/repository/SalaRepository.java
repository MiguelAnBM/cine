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
import com.comfenalco.cine.model.enums.EstadoSala;

import jakarta.persistence.LockModeType;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Sala> findByEstadoOrderByNombre(EstadoSala estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sala s WHERE s.id = :id")
    Optional<Sala> findByIdForUpdate(@Param("id") Long id);

    @Query("""
           SELECT new com.comfenalco.cine.dto.response.OcupacionSalaResponse(
                      s.nombre, COUNT(DISTINCT fu.id), COUNT(d.id))
           FROM Sala s
           LEFT JOIN Funcion fu ON fu.sala = s
                 AND fu.fecha BETWEEN :desde AND :hasta
           LEFT JOIN DetalleBoleto d ON d.funcion = fu
                 AND d.asientoActivo IS NOT NULL
                 AND d.factura.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
           GROUP BY s.id, s.nombre
           ORDER BY s.nombre
           """)
    List<OcupacionSalaResponse> ocupacionPorSala(@Param("desde") LocalDate desde,
                                                 @Param("hasta") LocalDate hasta);
}
