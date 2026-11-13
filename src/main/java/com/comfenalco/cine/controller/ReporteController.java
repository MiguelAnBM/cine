package com.comfenalco.cine.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.comfenalco.cine.dto.response.FuncionTaquilleraResponse;
import com.comfenalco.cine.dto.response.OcupacionSalaResponse;
import com.comfenalco.cine.dto.response.ResumenVentasResponse;
import com.comfenalco.cine.dto.response.VentaDiariaResponse;
import com.comfenalco.cine.dto.response.VentaPorPeliculaResponse;
import com.comfenalco.cine.dto.response.VentaPorProductoResponse;
import com.comfenalco.cine.dto.response.VentaPorTaquilleroResponse;
import com.comfenalco.cine.service.ReporteService;

/**
 * Controlador de la pantalla de Reportes.
 * Permite al Administrador consultar métricas consolidadas por fecha:
 * ingresos, ocupación por sala, películas más vistas, ventas por taquillero,
 * productos más vendidos y funciones más taquilleras.
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * Muestra las 6 secciones de reportes para la fecha indicada (o hoy por defecto).
     */
    @GetMapping
    public String verReportes(
            @RequestParam(name = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaConsulta,
            Model modelo) {

        // Si no se envía fecha, consultamos la fecha actual (hoy)
        LocalDate fecha = (fechaConsulta != null) ? fechaConsulta : LocalDate.now();

        // 1. Resumen de Ingresos y totales
        ResumenVentasResponse resumen = reporteService.resumen(fecha, fecha);

        // 2. Serie diaria de ventas (desglose del día)
        // 3. Ocupación por sala
        List<OcupacionSalaResponse> ocupacionSalas = reporteService.ocupacionPorSala(fecha, fecha);

        // 4. Películas más vistas
        List<VentaPorPeliculaResponse> peliculasMasVistas = reporteService.boletosPorPelicula(fecha, fecha, 5);

        // 5. Ventas asociadas a cada Taquillero
        List<VentaPorTaquilleroResponse> ventasTaquilleros = reporteService.ventasPorTaquillero(fecha, fecha);

        // 6. Productos más vendidos en cafetería
        List<VentaPorProductoResponse> productosVendidos = reporteService.productosMasVendidos(fecha, fecha, 5);

        // 7. Funciones más taquilleras
        List<FuncionTaquilleraResponse> funcionesTaquilleras = reporteService.funcionesMasTaquilleras(fecha, fecha, 5);

        // Pasamos todos los reportes a la vista
        modelo.addAttribute("fechaSeleccionada", fecha);
        modelo.addAttribute("resumen", resumen);
        modelo.addAttribute("ocupacionSalas", ocupacionSalas);
        modelo.addAttribute("peliculasMasVistas", peliculasMasVistas);
        modelo.addAttribute("ventasTaquilleros", ventasTaquilleros);
        modelo.addAttribute("productosVendidos", productosVendidos);
        modelo.addAttribute("funcionesTaquilleras", funcionesTaquilleras);

        return "reportes/index";
    }
}

