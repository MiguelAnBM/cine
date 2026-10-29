package com.comfenalco.cine.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.model.DetalleConfiteria;

public interface DetalleConfiteriaRepository extends Repository<DetalleConfiteria, Long> {

    boolean existsByProductoConfiteriaId(Long idProducto);   // ¿tiene ventas? (para desactivar en vez de borrar)

    @Query(
        """
            SELECT COALESCE(SUM(d.precioUnitario * d.cantidad), 0) FROM DetalleConfiteria d
            WHERE d.factura.fechaHora >= :desde AND d.factura.fechaHora < :hasta
        """)
    BigDecimal totalConfiteriaEnRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
