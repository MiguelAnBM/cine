package com.comfenalco.cine.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.model.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    // Listado con taquillero, sin consultas extra por factura
    @Query(
        """
            SELECT f FROM Factura f
            JOIN FETCH f.usuario
            WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
            ORDER BY f.fechaHora DESC
        """)
    List<Factura> findPorRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    Page<Factura> findByUsuarioIdOrderByFechaHoraDesc(Long idUsuario, Pageable pageable);

    // Detalle: usuario y boletos
    @Query(
        """
            SELECT f FROM Factura f
            JOIN FETCH f.usuario
            LEFT JOIN FETCH f.boletos
            WHERE f.id = :id
        """)
    Optional<Factura> findDetalleById(@Param("id") Long id);

    // Los productos van en otra consulta para evitar MultipleBagFetchException
    @Query(
        """
            SELECT DISTINCT f FROM Factura f
            LEFT JOIN FETCH f.productos
            WHERE f.id = :id
        """)
    Optional<Factura> findConProductosById(@Param("id") Long id);

    // Listado de facturas para administración, sin una consulta extra por cada taquillero
    @EntityGraph(attributePaths = "usuario")
    Page<Factura> findAll(Pageable pageable);
}