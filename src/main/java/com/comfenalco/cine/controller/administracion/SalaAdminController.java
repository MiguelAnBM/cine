package com.comfenalco.cine.controller.administracion;

import java.math.BigDecimal;
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

import com.comfenalco.cine.dto.request.SalaRequest;
import com.comfenalco.cine.dto.response.SalaResponse;
import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.model.enums.TipoSala;
import com.comfenalco.cine.service.SalaService;

import jakarta.validation.Valid;

/**
 * Controlador administrativo para el CRUD de Salas de Cine.
 * Permite listar salas, crear nuevas, modificar su nombre, tipo y precio de entrada,
 * y activarlas o desactivarlas lógicamente.
 */
@Controller
@RequestMapping("/administracion/salas")
public class SalaAdminController {

    private final SalaService salaService;

    public SalaAdminController(SalaService salaService) {
        this.salaService = salaService;
    }

    /**
     * Muestra el listado de todas las salas (activas e inactivas).
     */
    @GetMapping
    public String listarSalas(Model modelo) {
        List<SalaResponse> todasLasSalas = salaService.listar();
        modelo.addAttribute("salas", todasLasSalas);
        return "administracion/salas/lista";
    }

    /**
     * Muestra el formulario para registrar una nueva sala.
     */
    @GetMapping("/nueva")
    public String mostrarFormularioCrearSala(Model modelo) {
        if (!modelo.containsAttribute("salaFormulario")) {
            modelo.addAttribute("salaFormulario", new SalaRequest("", TipoSala.DOS_D, BigDecimal.ZERO));
        }
        modelo.addAttribute("tiposDeSala", TipoSala.values());
        modelo.addAttribute("esEdicion", false);
        return "administracion/salas/form";
    }

    /**
     * Procesa la creación de una nueva sala.
     */
    @PostMapping
    public String guardarNuevaSala(
            @Valid @ModelAttribute("salaFormulario") SalaRequest datosSala,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("tiposDeSala", TipoSala.values());
            modelo.addAttribute("esEdicion", false);
            return "administracion/salas/form";
        }

        salaService.crear(datosSala);
        mensajesFlash.addFlashAttribute("exito", "Sala «" + datosSala.nombre() + "» registrada exitosamente.");
        return "redirect:/administracion/salas";
    }

    /**
     * Muestra el formulario para editar nombre, tipo y precio del boleto de una sala.
     */
    @GetMapping("/{idSala}/editar")
    public String mostrarFormularioEditarSala(@PathVariable Long idSala, Model modelo) {
        SalaResponse salaExistente = salaService.buscarPorId(idSala);

        SalaRequest salaFormulario = new SalaRequest(
                salaExistente.nombre(),
                salaExistente.tipo(),
                salaExistente.precioBoleto()
        );

        modelo.addAttribute("salaFormulario", salaFormulario);
        modelo.addAttribute("idSala", idSala);
        modelo.addAttribute("tiposDeSala", TipoSala.values());
        modelo.addAttribute("esEdicion", true);

        return "administracion/salas/form";
    }

    /**
     * Guarda los cambios de la sala.
     */
    @PostMapping("/{idSala}")
    public String actualizarSala(
            @PathVariable Long idSala,
            @Valid @ModelAttribute("salaFormulario") SalaRequest datosActualizados,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("idSala", idSala);
            modelo.addAttribute("tiposDeSala", TipoSala.values());
            modelo.addAttribute("esEdicion", true);
            return "administracion/salas/form";
        }

        salaService.editar(idSala, datosActualizados);
        mensajesFlash.addFlashAttribute("exito", "Sala actualizada correctamente.");
        return "redirect:/administracion/salas";
    }

    /**
     * Activa o desactiva lógicamente una sala (borrado lógico).
     */
    @PostMapping("/{idSala}/estado")
    public String cambiarEstadoSala(
            @PathVariable Long idSala,
            @RequestParam(name = "nuevoEstado") EstadoSala nuevoEstado,
            RedirectAttributes mensajesFlash) {

        salaService.cambiarEstado(idSala, nuevoEstado);
        mensajesFlash.addFlashAttribute("exito", "El estado de la sala se actualizó a: " + nuevoEstado);
        return "redirect:/administracion/salas";
    }
}

