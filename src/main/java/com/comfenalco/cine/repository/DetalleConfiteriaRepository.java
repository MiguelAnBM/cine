package com.comfenalco.cine.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.dto.resumen.TotalPorUsuario;
import com.comfenalco.cine.dto.response.VentaPorProductoResponse;
import com.comfenalco.cine.model.DetalleConfiteria;

public interface DetalleConfiteriaRepository extends Repository<DetalleConfiteria, Long> {

    @Query("""
        SELECT COALESCE(SUM(d.precioUnitario * d.cantidad), 0) FROM DetalleConfiteria d
        WHERE d.factura.fechaHora >= :desde AND d.factura.fechaHora < :hasta
            AND d.factura.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
        """)
    BigDecimal totalConfiteriaEnRango(@Param("desde") LocalDateTime desde,
                                    @Param("hasta") LocalDateTime hasta);
    
    @Query("""
        SELECT new com.comfenalco.cine.dto.response.VentaPorProductoResponse(
                    p.nombre, SUM(d.cantidad), SUM(d.precioUnitario * d.cantidad))
        FROM DetalleConfiteria d
        JOIN d.factura f
        JOIN d.productoConfiteria p
        WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
            AND f.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
        GROUP BY p.id, p.nombre
        ORDER BY SUM(d.cantidad) DESC, p.nombre
        """)
    List<VentaPorProductoResponse> productosMasVendidos(@Param("desde") LocalDateTime desde,
                                                        @Param("hasta") LocalDateTime hasta,
                                                        Pageable pageable);

    // Filas: [año, mes, día, total]
    @Query("""
        SELECT year(f.fechaHora), month(f.fechaHora), day(f.fechaHora),
                SUM(d.precioUnitario * d.cantidad)
        FROM DetalleConfiteria d
        JOIN d.factura f
        WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
            AND f.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
        GROUP BY year(f.fechaHora), month(f.fechaHora), day(f.fechaHora)
        """)
    List<Object[]> totalConfiteriaPorDia(@Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta);

    // Total de confitería vendida por usuario (facturas emitidas, rango por fecha de venta)
    @Query("""
        SELECT new com.comfenalco.cine.dto.resumen.TotalPorUsuario(
                    f.usuario.id, SUM(d.precioUnitario * d.cantidad))
        FROM DetalleConfiteria d
        JOIN d.factura f
        WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
            AND f.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
        GROUP BY f.usuario.id
        """)
    List<TotalPorUsuario> totalConfiteriaPorUsuario(@Param("desde") LocalDateTime desde,
                                                    @Param("hasta") LocalDateTime hasta);
}