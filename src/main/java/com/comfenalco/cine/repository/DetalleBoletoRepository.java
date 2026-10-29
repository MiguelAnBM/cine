package com.comfenalco.cine.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.model.DetalleBoleto;
import com.comfenalco.cine.model.Factura;

public interface DetalleBoletoRepository extends Repository<DetalleBoleto, Long> {

    boolean existsByFuncionIdAndCodigoAsiento(Long idFuncion, String codigoAsiento);
    
    boolean existsByFuncionId(Long idFuncion);   // ¿se puede borrar/modificar esta función?

    // Mapa de asientos: solo los códigos ocupados
    @Query("SELECT d.codigoAsiento FROM DetalleBoleto d WHERE d.funcion.id = :idFuncion")
    List<String> findAsientosOcupados(@Param("idFuncion") Long idFuncion);

    // Facturas afectadas al cancelar una función
    @Query("SELECT DISTINCT d.factura FROM DetalleBoleto d WHERE d.funcion.id = :idFuncion")
    List<Factura> findFacturasAfectadas(@Param("idFuncion") Long idFuncion);

    // Reportes
    @Query(
        """
            SELECT COALESCE(SUM(d.precioUnitario), 0) FROM DetalleBoleto d
            WHERE d.factura.fechaHora >= :desde AND d.factura.fechaHora < :hasta
        """)
    BigDecimal totalBoletosEnRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}