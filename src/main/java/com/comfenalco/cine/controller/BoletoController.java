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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.comfenalco.cine.dto.request.VentaRequest;
import com.comfenalco.cine.dto.response.AsientoResponse;
import com.comfenalco.cine.dto.response.FacturaResponse;
import com.comfenalco.cine.dto.response.FuncionResponse;
import com.comfenalco.cine.model.enums.MetodoPago;
import com.comfenalco.cine.service.DetalleBoletoService;
import com.comfenalco.cine.service.FuncionService;
import com.comfenalco.cine.service.UsuarioService;
import com.comfenalco.cine.service.VentaService;

import jakarta.validation.Valid;

/**
 * Controlador para la venta de boletos en mostrador.
 * Separado en dos rutas limpias: una para seleccionar la función (/boletos)
 * y otra para consultar el mapa de asientos de la función seleccionada (/boletos/{idFuncion}).
 */
@Controller
@RequestMapping("/boletos")
public class BoletoController {

    private final FuncionService funcionService;
    private final DetalleBoletoService detalleBoletoService;
    private final UsuarioService usuarioService;
    private final VentaService ventaService;

    public BoletoController(FuncionService funcionService,
                            DetalleBoletoService detalleBoletoService,
                            UsuarioService usuarioService,
                            VentaService ventaService) {
        this.funcionService = funcionService;
        this.detalleBoletoService = detalleBoletoService;
        this.usuarioService = usuarioService;
        this.ventaService = ventaService;
    }

    /**
     * Ruta 1: Muestra la pantalla inicial con el selector de funciones disponibles.
     */
    @GetMapping
    public String mostrarSelectorDeBoletos(Model modelo) {
        cargarDatosBase(modelo);
        return "boletos/index";
    }

    /**
     * Ruta 2: Muestra el mapa de 80 asientos (libres/ocupados) para la función especificada.
     */
    @GetMapping("/{idFuncion}")
    public String mostrarMapaDeBoletos(@PathVariable Long idFuncion, Model modelo) {
        cargarDatosBase(modelo);

        funcionService.validarVendible(idFuncion);

        FuncionResponse funcionSeleccionada = funcionService.buscarPorId(idFuncion);
        List<AsientoResponse> mapaDeAsientos = detalleBoletoService.mapaDeAsientos(idFuncion);

        modelo.addAttribute("funcionSeleccionada", funcionSeleccionada);
        modelo.addAttribute("idFuncionSeleccionada", idFuncion);
        modelo.addAttribute("mapaDeAsientos", mapaDeAsientos);

        return "boletos/index";
    }

    /**
     * Procesa la compra de los boletos seleccionados y redirige a la función actual.
     */
    @PostMapping("/venta")
    public String procesarVentaDeBoletos(
            @Valid @ModelAttribute("ventaFormulario") VentaRequest solicitud,
            BindingResult resultadoValidacion,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            mensajesFlash.addFlashAttribute("error", "Corrige los errores del formulario antes de continuar.");
            return "redirect:/boletos/" + solicitud.idFuncion();
        }

        FacturaResponse facturaGenerada = ventaService.vender(solicitud.idTaquillero(), solicitud);

        mensajesFlash.addFlashAttribute("facturaEmitida", facturaGenerada);

        return "redirect:/boletos/" + solicitud.idFuncion();
    }

    /**
     * Carga las listas desplegables comunes para la vista de boletos.
     */
    private void cargarDatosBase(Model modelo) {
        if (!modelo.containsAttribute("ventaFormulario")) {
            modelo.addAttribute("ventaFormulario", new VentaRequest(null, null, null, null, null, null, null));
        }
        modelo.addAttribute("funcionesDisponibles", funcionService.proximas(50));
        modelo.addAttribute("taquillerosActivos", usuarioService.listarTaquillerosActivos());
        modelo.addAttribute("metodosDePago", MetodoPago.values());
    }
}
