package com.comfenalco.cine.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.comfenalco.cine.dto.response.FuncionResponse;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.dto.response.ResumenVentasResponse;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.service.FuncionService;
import com.comfenalco.cine.service.PeliculaService;
import com.comfenalco.cine.service.ReporteService;
import com.comfenalco.cine.service.SalaService;

/**
 * Controlador de la pantalla principal de Inicio (Dashboard).
 * Es una página 100% pública para clientes y personal del cine.
 */
@Controller
public class InicioController {

    private final FuncionService funcionService;
    private final PeliculaService peliculaService;
    private final ReporteService reporteService;
    private final SalaService salaService;

    public InicioController(FuncionService funcionService,
                            PeliculaService peliculaService,
                            ReporteService reporteService,
                            SalaService salaService) {
        this.funcionService = funcionService;
        this.peliculaService = peliculaService;
        this.reporteService = reporteService;
        this.salaService = salaService;
    }

    @GetMapping({"/", "/inicio"})
    public String mostrarInicio(Model modelo) {
        LocalDate hoy = LocalDate.now();

        // 1. Obtenemos las películas de la cartelera (activas)
        List<PeliculaResponse> peliculasEnCartelera = peliculaService.listarPorEstado(EstadoPelicula.ACTIVA);
        if (peliculasEnCartelera.isEmpty()) {
            peliculasEnCartelera = peliculaService.listar();
        }

        // 2. Obtenemos las próximas 4 funciones del día actual que aún no comienzan
        List<FuncionResponse> proximasFunciones = funcionService.proximasDeHoy(4);

        // 3. Obtenemos el resumen del sistema para el día de hoy
        ResumenVentasResponse resumenDelDia = reporteService.resumen(hoy, hoy);

        // 4. Obtenemos el total de funciones programadas o activas para el día de hoy
        long funcionesParaHoy = funcionService.contarFuncionesDelDia(hoy);

        // 5. Obtenemos las salas activas al momento
        int salasActivas = salaService.listarPorEstado(EstadoSala.ACTIVA).size();

        // Pasamos los datos a la plantilla de Thymeleaf
        modelo.addAttribute("peliculasEnCartelera", peliculasEnCartelera);
        modelo.addAttribute("proximasFunciones", proximasFunciones);
        modelo.addAttribute("resumen", resumenDelDia);
        modelo.addAttribute("funcionesParaHoy", funcionesParaHoy);
        modelo.addAttribute("salasActivas", salasActivas);

        return "inicio";
    }
}

