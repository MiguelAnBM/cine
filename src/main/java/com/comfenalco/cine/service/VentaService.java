package com.comfenalco.cine.service;

import java.time.LocalDate;
import java.util.List;

import com.comfenalco.cine.dto.response.FacturaResponse;
import com.comfenalco.cine.dto.response.FacturaResumenResponse;
import com.comfenalco.cine.dto.request.VentaRequest;

public interface VentaService {

    // idUsuario es el taquillero autenticado. Nunca debe venir del formulario
    FacturaResponse vender(Long idUsuario, VentaRequest request);

    FacturaResponse buscarPorId(Long id);

    List<FacturaResumenResponse> listarPorRango(LocalDate desde, LocalDate hasta);

    FacturaResponse anular(Long idUsuario, Long idFactura);
}