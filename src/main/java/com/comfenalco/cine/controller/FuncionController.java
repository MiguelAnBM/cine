package com.comfenalco.cine.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.comfenalco.cine.dto.request.FuncionRequest;
import com.comfenalco.cine.dto.response.FuncionResponse;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.dto.response.SalaResponse;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.service.FuncionService;
import com.comfenalco.cine.service.PeliculaService;
import com.comfenalco.cine.service.SalaService;

import jakarta.validation.Valid;

/**
 * Controlador para la programación y consulta de funciones.
 * Permite listar funciones con paginación, programar nuevas, editarlas,
 * ver el detalle
 */
@Controller
@RequestMapping("/funciones")
public class FuncionController {

    private final FuncionService funcionService;
    private final PeliculaService peliculaService;
    private final SalaService salaService;

    public FuncionController(FuncionService funcionService,
                             PeliculaService peliculaService,
                             SalaService salaService) {
        this.funcionService = funcionService;
        this.peliculaService = peliculaService;
        this.salaService = salaService;
    }

    /**
     * Muestra el listado de funciones paginadas (10 funciones por página).
     */
    @GetMapping
    public String listarFunciones(
            @RequestParam(name = "pagina", defaultValue = "0") int numeroDePagina,
            Model modelo) {

        Page<FuncionResponse> paginaDeFunciones = funcionService.listar(PageRequest.of(Math.max(0, numeroDePagina), 10));

        modelo.addAttribute("paginaFunciones", paginaDeFunciones);
        modelo.addAttribute("paginaActual", paginaDeFunciones.getNumber());
        modelo.addAttribute("totalPaginas", paginaDeFunciones.getTotalPages());

        return "funciones/lista";
    }

    /**
     * Muestra el detalle de una función en particular.
     */
    @GetMapping("/{idFuncion}")
    public String verDetalleFuncion(@PathVariable Long idFuncion, Model modelo) {
        FuncionResponse funcion = funcionService.buscarPorId(idFuncion);
        modelo.addAttribute("funcion", funcion);
        return "funciones/detalle";
    }

    /**
     * Muestra el formulario para programar una nueva función.
     */
    @GetMapping("/nueva")
    public String mostrarFormularioCrear(Model modelo) {
        if (!modelo.containsAttribute("funcionFormulario")) {
            modelo.addAttribute("funcionFormulario", new FuncionRequest(null, null, null, null));
        }
        cargarPeliculasYSalasActivas(modelo);
        modelo.addAttribute("esEdicion", false);
        return "funciones/form";
    }

    /**
     * Guarda la nueva función programada.
     */
    @PostMapping
    public String guardarFuncion(
            @Valid @ModelAttribute("funcionFormulario") FuncionRequest datosFuncion,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            cargarPeliculasYSalasActivas(modelo);
            modelo.addAttribute("esEdicion", false);
            return "funciones/form";
        }

        funcionService.crear(datosFuncion);
        mensajesFlash.addFlashAttribute("exito", "Función programada exitosamente.");
        return "redirect:/funciones";
    }

    /**
     * Muestra el formulario para editar una función existente.
     */
    @GetMapping("/{idFuncion}/editar")
    public String mostrarFormularioEditar(@PathVariable Long idFuncion, Model modelo) {
        FuncionResponse funcionExistente = funcionService.buscarPorId(idFuncion);

        FuncionRequest funcionFormulario = new FuncionRequest(
                funcionExistente.idPelicula(),
                funcionExistente.idSala(),
                funcionExistente.fecha(),
                funcionExistente.horaInicio()
        );

        modelo.addAttribute("funcionFormulario", funcionFormulario);
        modelo.addAttribute("idFuncion", idFuncion);
        modelo.addAttribute("esEdicion", true);
        cargarPeliculasYSalasActivas(modelo);

        return "funciones/form";
    }

    /**
     * Guarda las modificaciones de la función.
     */
    @PostMapping("/{idFuncion}")
    public String actualizarFuncion(
            @PathVariable Long idFuncion,
            @Valid @ModelAttribute("funcionFormulario") FuncionRequest datosActualizados,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("idFuncion", idFuncion);
            modelo.addAttribute("esEdicion", true);
            cargarPeliculasYSalasActivas(modelo);
            return "funciones/form";
        }

        funcionService.editar(idFuncion, datosActualizados);
        mensajesFlash.addFlashAttribute("exito", "Función modificada correctamente.");
        return "redirect:/funciones";
    }

    /**
     * Carga las películas y salas activas para mostrarlas en los desplegables de selección.
     */
    private void cargarPeliculasYSalasActivas(Model modelo) {
        List<PeliculaResponse> peliculasActivas = peliculaService.listarPorEstado(EstadoPelicula.ACTIVA);
        List<SalaResponse> salasActivas = salaService.listarPorEstado(EstadoSala.ACTIVA);

        modelo.addAttribute("listaPeliculas", peliculasActivas);
        modelo.addAttribute("listaSalas", salasActivas);
    }
}

