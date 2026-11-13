package com.comfenalco.cine.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.dto.resumen.ConteoPorFuncion;
import com.comfenalco.cine.dto.response.FuncionTaquilleraResponse;
import com.comfenalco.cine.dto.resumen.TotalPorUsuario;
import com.comfenalco.cine.dto.response.VentaPorPeliculaResponse;
import com.comfenalco.cine.model.DetalleBoleto;

// Extiende Repository para no acceder a borrar ni crear
public interface DetalleBoletoRepository extends Repository<DetalleBoleto, Long> {

       // ¿La función tiene ventas reales? Los boletos de facturas anuladas no cuentan
       boolean existsByFuncionIdAndAsientoActivoIsNotNull(Long idFuncion);

       // Mapa de asientos: solo los vigentes
       @Query("""
              SELECT d.asientoActivo FROM DetalleBoleto d
              WHERE d.funcion.id = :idFuncion AND d.asientoActivo IS NOT NULL
              """)
       List<String> findAsientosOcupados(@Param("idFuncion") Long idFuncion);

    // Reportes: solo facturas emitidas
       @Query("""
              SELECT COALESCE(SUM(d.precioUnitario), 0) FROM DetalleBoleto d
              WHERE d.factura.fechaHora >= :desde AND d.factura.fechaHora < :hasta
              AND d.factura.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
              """)
       BigDecimal totalBoletosEnRango(@Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta);
       @Query("""
              SELECT COUNT(d) FROM DetalleBoleto d
              WHERE d.factura.fechaHora >= :desde AND d.factura.fechaHora < :hasta
              AND d.factura.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
              """)
       long contarBoletosEnRango(@Param("desde") LocalDateTime desde,
                            @Param("hasta") LocalDateTime hasta);

       @Query("""
              SELECT new com.comfenalco.cine.dto.response.VentaPorPeliculaResponse(
                     p.titulo, COUNT(d), SUM(d.precioUnitario))
              FROM DetalleBoleto d
              JOIN d.factura f
              JOIN d.funcion fu
              JOIN fu.pelicula p
              WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
              AND f.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
              GROUP BY p.id, p.titulo
              ORDER BY COUNT(d) DESC, p.titulo
              """)
       List<VentaPorPeliculaResponse> boletosPorPelicula(@Param("desde") LocalDateTime desde,
                                                        @Param("hasta") LocalDateTime hasta,
                                                        Pageable pageable);

       // Filas: [año, mes, día, total]
       @Query("""
              SELECT year(f.fechaHora), month(f.fechaHora), day(f.fechaHora), SUM(d.precioUnitario)
              FROM DetalleBoleto d
              JOIN d.factura f
              WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
              AND f.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
              GROUP BY year(f.fechaHora), month(f.fechaHora), day(f.fechaHora)
              """)
       List<Object[]> totalBoletosPorDia(@Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta);

    // Boletos vigentes de una función
    long countByFuncionIdAndAsientoActivoIsNotNull(Long idFuncion);

    // Boletos vigentes de varias funciones en una sola consulta (las que no tienen no aparecen)
    @Query("""
           SELECT new com.comfenalco.cine.dto.resumen.ConteoPorFuncion(d.funcion.id, COUNT(d))
           FROM DetalleBoleto d
           WHERE d.funcion.id IN :ids AND d.asientoActivo IS NOT NULL
           GROUP BY d.funcion.id
           """)
    List<ConteoPorFuncion> contarBoletosPorFuncion(@Param("ids") Collection<Long> ids);

    // Funciones más taquilleras: boletos vigentes por función vendidos en el rango, solo funciones de la fecha filtrada
    @Query("""
           SELECT new com.comfenalco.cine.dto.response.FuncionTaquilleraResponse(
                      p.titulo, s.nombre, f.fecha, f.horaInicio, COUNT(d), SUM(d.precioUnitario))
           FROM DetalleBoleto d
           JOIN d.factura fac
           JOIN d.funcion f
           JOIN f.pelicula p
           JOIN f.sala s
           WHERE fac.fechaHora >= :desde AND fac.fechaHora < :hasta
             AND fac.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
             AND d.asientoActivo IS NOT NULL
             AND f.fecha = :fechaFuncion
           GROUP BY f.id, p.titulo, s.nombre, f.fecha, f.horaInicio
           ORDER BY COUNT(d) DESC, f.fecha DESC, f.horaInicio DESC
           """)
    List<FuncionTaquilleraResponse> funcionesMasTaquilleras(@Param("desde") LocalDateTime desde,
                                                            @Param("hasta") LocalDateTime hasta,
                                                            @Param("fechaFuncion") java.time.LocalDate fechaFuncion,
                                                            Pageable pageable);

    // Total de boletos vendidos por usuario (facturas emitidas, rango por fecha de venta)
    @Query("""
           SELECT new com.comfenalco.cine.dto.resumen.TotalPorUsuario(f.usuario.id, SUM(d.precioUnitario))
           FROM DetalleBoleto d
           JOIN d.factura f
           WHERE f.fechaHora >= :desde AND f.fechaHora < :hasta
             AND f.estado = com.comfenalco.cine.model.enums.EstadoFactura.EMITIDA
           GROUP BY f.usuario.id
           """)
    List<TotalPorUsuario> totalBoletosPorUsuario(@Param("desde") LocalDateTime desde,
                                                 @Param("hasta") LocalDateTime hasta);
}