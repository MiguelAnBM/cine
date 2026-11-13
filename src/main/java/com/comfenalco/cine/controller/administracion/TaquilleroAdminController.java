package com.comfenalco.cine.controller.administracion;

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

import com.comfenalco.cine.dto.request.UsuarioCrearRequest;
import com.comfenalco.cine.dto.request.UsuarioEditarRequest;
import com.comfenalco.cine.dto.response.UsuarioResponse;
import com.comfenalco.cine.model.enums.EstadoUsuario;
import com.comfenalco.cine.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador administrativo para la gestión de Taquilleros (personal de mostrador).
 * Permite dar de alta taquilleros, editar su nombre, y activarlos/desactivarlos.
 * No hay username ni contraseña: los taquilleros no inician sesión.
 */
@Controller
@RequestMapping("/administracion/taquilleros")
public class TaquilleroAdminController {

    private final UsuarioService usuarioService;

    public TaquilleroAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Muestra la lista de todos los taquilleros registrados en el sistema.
     */
    @GetMapping
    public String listarTaquilleros(Model modelo) {
        List<UsuarioResponse> taquilleros = usuarioService.listarTaquilleros();
        modelo.addAttribute("taquilleros", taquilleros);
        return "administracion/taquilleros/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo taquillero.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model modelo) {
        if (!modelo.containsAttribute("taquilleroFormulario")) {
            modelo.addAttribute("taquilleroFormulario", new UsuarioCrearRequest(""));
        }
        modelo.addAttribute("esEdicion", false);
        return "administracion/taquilleros/form";
    }

    /**
     * Guarda el nuevo taquillero.
     */
    @PostMapping
    public String guardarNuevoTaquillero(
            @Valid @ModelAttribute("taquilleroFormulario") UsuarioCrearRequest datosTaquillero,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("esEdicion", false);
            return "administracion/taquilleros/form";
        }

        usuarioService.crear(datosTaquillero);
        mensajesFlash.addFlashAttribute("exito",
                "Taquillero «" + datosTaquillero.nombreCompleto() + "» registrado con éxito.");

        return "redirect:/administracion/taquilleros";
    }

    /**
     * Muestra el formulario para editar nombre del taquillero.
     */
    @GetMapping("/{idTaquillero}/editar")
    public String mostrarFormularioEditar(@PathVariable Long idTaquillero, Model modelo) {
        UsuarioResponse taquilleroExistente = usuarioService.buscarPorId(idTaquillero);

        UsuarioEditarRequest formulario = new UsuarioEditarRequest(
                taquilleroExistente.nombreCompleto()
        );

        modelo.addAttribute("taquilleroFormulario", formulario);
        modelo.addAttribute("idTaquillero", idTaquillero);
        modelo.addAttribute("esEdicion", true);

        return "administracion/taquilleros/form";
    }

    /**
     * Procesa la edición del taquillero.
     */
    @PostMapping("/{idTaquillero}")
    public String actualizarTaquillero(
            @PathVariable Long idTaquillero,
            @Valid @ModelAttribute("taquilleroFormulario") UsuarioEditarRequest datosActualizados,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("idTaquillero", idTaquillero);
            modelo.addAttribute("esEdicion", true);
            return "administracion/taquilleros/form";
        }

        usuarioService.editar(idTaquillero, datosActualizados);
        mensajesFlash.addFlashAttribute("exito", "Datos del taquillero actualizados correctamente.");

        return "redirect:/administracion/taquilleros";
    }

    /**
     * Activa o desactiva a un taquillero.
     */
    @PostMapping("/{idTaquillero}/estado")
    public String cambiarEstadoTaquillero(
            @PathVariable Long idTaquillero,
            @RequestParam(name = "nuevoEstado") EstadoUsuario nuevoEstado,
            RedirectAttributes mensajesFlash) {

        usuarioService.cambiarEstado(idTaquillero, nuevoEstado);
        mensajesFlash.addFlashAttribute("exito",
                "El estado del taquillero cambió a: " + nuevoEstado);

        return "redirect:/administracion/taquilleros";
    }
}
