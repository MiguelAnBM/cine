package com.comfenalco.cine.service;

import java.time.LocalDate;
import java.util.List;

import com.comfenalco.cine.dto.response.FuncionTaquilleraResponse;
import com.comfenalco.cine.dto.response.OcupacionSalaResponse;
import com.comfenalco.cine.dto.response.ResumenVentasResponse;
import com.comfenalco.cine.dto.response.VentaDiariaResponse;
import com.comfenalco.cine.dto.response.VentaPorPeliculaResponse;
import com.comfenalco.cine.dto.response.VentaPorProductoResponse;
import com.comfenalco.cine.dto.response.VentaPorTaquilleroResponse;

public interface ReporteService {

    ResumenVentasResponse resumen(LocalDate desde, LocalDate hasta);

    List<VentaPorPeliculaResponse> boletosPorPelicula(LocalDate desde, LocalDate hasta, int limite);

    List<VentaPorProductoResponse> productosMasVendidos(LocalDate desde, LocalDate hasta, int limite);

    // Una fila por cada día del rango, incluso los días sin ventas (con ceros)
    List<VentaDiariaResponse> ventasPorDia(LocalDate desde, LocalDate hasta);

    // Por sala: funciones del rango, boletos vendidos, capacidad (80 por función) y porcentaje de ocupación
    List<OcupacionSalaResponse> ocupacionPorSala(LocalDate desde, LocalDate hasta);

    // Facturas y totales por taquillero, de mayor a menor venta
    List<VentaPorTaquilleroResponse> ventasPorTaquillero(LocalDate desde, LocalDate hasta);

    // Funciones con más boletos vendidos (rango por fecha de la función)
    List<FuncionTaquilleraResponse> funcionesMasTaquilleras(LocalDate desde, LocalDate hasta, int limite);
}