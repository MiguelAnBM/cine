package com.comfenalco.cine.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.dto.resumen.ConteoPorUsuario;
import com.comfenalco.cine.model.Factura;
import com.comfenalco.cine.model.enums.EstadoFactura;

// Extiende Repository (no JpaRepository) para no exponer delete: las facturas no se borran, se anulan
public interface FacturaRepository extends Repository<Factura, Long> {

    <S extends Factura> S save(S factura);

    // Listado con taquillero, sin consultas extra por factura
    @Query("""
           SELECT f FROM Factura f
           JOIN FETCH f.usuario
           WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
           ORDER BY f.fechaHora DESC
           """)
    List<Factura> findPorRango(@Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta);

    // Comprobante, parte 1: usuario y boletos con su función, película y sala
    @Query("""
           SELECT f FROM Factura f
           JOIN FETCH f.usuario
           LEFT JOIN FETCH f.boletos b
           LEFT JOIN FETCH b.funcion fu
           LEFT JOIN FETCH fu.pelicula
           LEFT JOIN FETCH fu.sala
           WHERE f.id = :id
           """)
    Optional<Factura> findDetalleById(@Param("id") Long id);

    // Comprobante, parte 2: productos (en otra consulta para evitar MultipleBagFetchException)
    @Query("""
           SELECT DISTINCT f FROM Factura f
           LEFT JOIN FETCH f.productos p
           LEFT JOIN FETCH p.productoConfiteria
           WHERE f.id = :id
           """)
    Optional<Factura> findConProductosById(@Param("id") Long id);

    long countByEstadoAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
        EstadoFactura estado, LocalDateTime desde, LocalDateTime hasta);

    // Facturas emitidas por usuario en un rango de fechas (reporte de ventas por taquillero)
    @Query("""
           SELECT new com.comfenalco.cine.dto.resumen.ConteoPorUsuario(u.id, u.nombreCompleto, COUNT(f))
           FROM Factura f
           JOIN f.usuario u
           WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
             AND f.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
           GROUP BY u.id, u.nombreCompleto
           ORDER BY u.nombreCompleto
           """)
    List<ConteoPorUsuario> facturasPorUsuario(@Param("desde") LocalDateTime desde,
                                              @Param("hasta") LocalDateTime hasta);
}