package com.comfenalco.cine.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.comfenalco.cine.dto.response.FacturaAdminResponse;
import com.comfenalco.cine.dto.response.FacturaResponse;
import com.comfenalco.cine.dto.response.FacturaResumenResponse;
import com.comfenalco.cine.dto.request.VentaRequest;
import com.comfenalco.cine.model.enums.EstadoFactura;

public interface VentaService {

    // idUsuario es el taquillero autenticado. Nunca debe venir del formulario
    FacturaResponse vender(Long idUsuario, VentaRequest request);

    FacturaResponse buscarPorId(Long id);

    // Lista las facturas emitidas por un taquillero paginadas para que el administrador pueda anularlas
    Page<FacturaResumenResponse> listarEmitidasPorTaquillero(Long idTaquillero, Pageable pageable);

    // Todas las facturas con filtros opcionales para el panel de administración
    Page<FacturaAdminResponse> listarFacturasFiltradas(Long idTaquillero, EstadoFactura estado,
                                                       LocalDateTime desde, LocalDateTime hasta,
                                                       Pageable pageable);

    FacturaResponse anular(Long idUsuario, Long idFactura);

    /** Genera el texto plano con el comprobante de la factura para descarga o impresión. */
    String generarTextoFactura(FacturaResponse factura);
}