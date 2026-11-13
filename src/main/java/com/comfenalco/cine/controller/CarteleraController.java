package com.comfenalco.cine.controller;

import java.util.List;

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

import com.comfenalco.cine.dto.request.PeliculaRequest;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.model.enums.Clasificacion;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.Genero;
import com.comfenalco.cine.service.PeliculaService;

import jakarta.validation.Valid;

/**
 * Controlador para la cartelera de películas.
 * Permite listar películas activas, crear nuevas, editarlas y desactivarlas.
 */
@Controller
@RequestMapping("/cartelera")
public class CarteleraController {

    private final PeliculaService peliculaService;

    public CarteleraController(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    /**
     * Muestra la tabla de películas en cartelera.
     * Por defecto muestra todas; permite filtrar solo activas.
     */
    @GetMapping
    public String listarPeliculasEnCartelera(
            @RequestParam(name = "estado", required = false) EstadoPelicula estado,
            Model modelo) {

        List<PeliculaResponse> peliculas = (estado == null)
                ? peliculaService.listar()
                : peliculaService.listarPorEstado(estado);

        modelo.addAttribute("peliculas", peliculas);
        modelo.addAttribute("estadoFiltro", estado);
        return "cartelera/lista";
    }

    /**
     * Muestra el formulario vacío para registrar una nueva película.
     */
    @GetMapping("/nueva")
    public String mostrarFormularioCrearPelicula(Model modelo) {
        if (!modelo.containsAttribute("peliculaFormulario")) {
            // Inicializamos el formulario vacío
            modelo.addAttribute("peliculaFormulario", new PeliculaRequest("", "", "", Clasificacion.TODO_PUBLICO, Genero.ACCION, ""));
        }
        cargarListasDesplegables(modelo);
        modelo.addAttribute("esEdicion", false);
        return "cartelera/form";
    }

    /**
     * Procesa el guardado de una nueva película.
     */
    @PostMapping
    public String guardarNuevaPelicula(
            @Valid @ModelAttribute("peliculaFormulario") PeliculaRequest datosPelicula,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            cargarListasDesplegables(modelo);
            modelo.addAttribute("esEdicion", false);
            return "cartelera/form";
        }

        peliculaService.crear(datosPelicula);
        mensajesFlash.addFlashAttribute("exito", "Película «" + datosPelicula.titulo() + "» agregada correctamente a la cartelera.");
        return "redirect:/cartelera";
    }

    /**
     * Muestra el formulario precargado con los datos de una película existente para editarla.
     */
    @GetMapping("/{idPelicula}/editar")
    public String mostrarFormularioEditarPelicula(@PathVariable Long idPelicula, Model modelo) {
        PeliculaResponse peliculaExistente = peliculaService.buscarPorId(idPelicula);

        PeliculaRequest peliculaFormulario = new PeliculaRequest(
                peliculaExistente.titulo(),
                peliculaExistente.sinopsis(),
                peliculaExistente.duracion(),
                peliculaExistente.clasificacion(),
                peliculaExistente.genero(),
                peliculaExistente.imagenUrl()
        );

        modelo.addAttribute("peliculaFormulario", peliculaFormulario);
        modelo.addAttribute("idPelicula", idPelicula);
        modelo.addAttribute("esEdicion", true);
        cargarListasDesplegables(modelo);

        return "cartelera/form";
    }

    /**
     * Procesa la actualización de los datos de una película.
     */
    @PostMapping("/{idPelicula}")
    public String actualizarPelicula(
            @PathVariable Long idPelicula,
            @Valid @ModelAttribute("peliculaFormulario") PeliculaRequest datosActualizados,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("idPelicula", idPelicula);
            modelo.addAttribute("esEdicion", true);
            cargarListasDesplegables(modelo);
            return "cartelera/form";
        }

        peliculaService.editar(idPelicula, datosActualizados);
        mensajesFlash.addFlashAttribute("exito", "Película actualizada correctamente.");
        return "redirect:/cartelera";
    }

    /**
     * Activa o desactiva lógicamente una película según el parámetro recibido.
     * Se usa el mismo endpoint para activar (nuevoEstado=ACTIVA) y desactivar (nuevoEstado=INACTIVA).
     */
    @PostMapping("/{idPelicula}/estado")
    public String cambiarEstadoPelicula(
            @PathVariable Long idPelicula,
            @RequestParam(name = "nuevoEstado") EstadoPelicula nuevoEstado,
            RedirectAttributes mensajesFlash) {

        peliculaService.cambiarEstado(idPelicula, nuevoEstado);

        String mensajeEstado = (nuevoEstado == EstadoPelicula.ACTIVA)
                ? "La película fue reactivada en la cartelera."
                : "La película fue retirada de la cartelera.";

        mensajesFlash.addFlashAttribute("exito", mensajeEstado);
        return "redirect:/cartelera";
    }

    /**
     * Carga las clasificaciones y géneros disponibles para los selectores del formulario.
     */
    private void cargarListasDesplegables(Model modelo) {
        modelo.addAttribute("listaClasificaciones", Clasificacion.values());
        modelo.addAttribute("listaGeneros", Genero.values());
    }
}

