package com.comfenalco.cine.controller.administracion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.comfenalco.cine.dto.response.FacturaAdminResponse;
import com.comfenalco.cine.dto.response.FacturaResponse;
import com.comfenalco.cine.dto.response.FacturaResumenResponse;
import com.comfenalco.cine.dto.response.UsuarioResponse;
import com.comfenalco.cine.model.enums.EstadoFactura;
import com.comfenalco.cine.service.UsuarioService;
import com.comfenalco.cine.service.VentaService;

/**
 * Controlador principal del panel de Administración y del módulo de Anulaciones.
 * Exclusivo para usuarios con rol ADMINISTRADOR.
 */
@Controller
@RequestMapping("/administracion")
public class AdministracionController {

    // Tamaño de página: limita cuántas facturas se cargan por consulta (evita cargar miles)
    private static final int TAMANO_PAGINA = 10;

    private final UsuarioService usuarioService;
    private final VentaService ventaService;

    public AdministracionController(UsuarioService usuarioService, VentaService ventaService) {
        this.usuarioService = usuarioService;
        this.ventaService = ventaService;
    }

    /**
     * Muestra el menú principal de Administración (Salas, Taquilleros, Anulaciones).
     */
    @GetMapping
    public String mostrarPanelPrincipal() {
        return "administracion/index";
    }

    /**
     * Muestra la pantalla de Anulación de facturas con paginación.
     * Permite filtrar facturas emitidas por taquillero y navegar páginas de 10 facturas.
     */
    @GetMapping("/anulaciones")
    public String mostrarPantallaAnulaciones(
            @RequestParam(name = "idTaquillero", required = false) Long idTaquillero,
            @RequestParam(name = "pagina", required = false, defaultValue = "0") int pagina,
            Model modelo) {

        // Cargamos todos los taquilleros (incluidos inactivos para consultar historial)
        List<UsuarioResponse> todosLosTaquilleros = usuarioService.listarTaquilleros();
        modelo.addAttribute("taquilleros", todosLosTaquilleros);
        modelo.addAttribute("idTaquilleroSeleccionado", idTaquillero);

        // Si no hay taquillero seleccionado, no paginamos nada
        if (idTaquillero == null) {
            modelo.addAttribute("paginaFacturas", Page.empty());
            return "administracion/anulaciones";
        }

        // Página de facturas: ordena por fecha descendente, tamaño fijo
        PageRequest pageable = PageRequest.of(
                Math.max(0, pagina),
                TAMANO_PAGINA,
                Sort.by(Sort.Direction.DESC, "fechaHora"));

        Page<FacturaResumenResponse> paginaFacturas =
                ventaService.listarEmitidasPorTaquillero(idTaquillero, pageable);

        modelo.addAttribute("paginaFacturas", paginaFacturas);
        modelo.addAttribute("paginaActual", paginaFacturas.getNumber());

        return "administracion/anulaciones";
    }

    /**
     * Procesa la anulación de una factura emitida.
     * La regla de negocio exige que sea autorizada por el Administrador autenticado.
     */
    @PostMapping("/anulaciones/{idFactura}/anular")
    public String anularFactura(
            @PathVariable Long idFactura,
            @RequestParam(name = "idTaquillero", required = false) Long idTaquillero,
            @RequestParam(name = "pagina", required = false, defaultValue = "0") int pagina,
            Authentication autenticacion,
            RedirectAttributes mensajesFlash) {

        // Obtenemos los datos del administrador que autoriza la anulación
        UsuarioResponse administrador = usuarioService.buscarPorUsername(autenticacion.getName());

        FacturaResponse facturaAnulada = ventaService.anular(administrador.id(), idFactura);

        mensajesFlash.addFlashAttribute("exito",
                "Factura #" + facturaAnulada.id() + " anulada correctamente. Los asientos han sido liberados.");

        // Redirige manteniendo el filtro y la página actual
        StringBuilder redirect = new StringBuilder("redirect:/administracion/anulaciones");
        String separador = "?";

        if (idTaquillero != null) {
            redirect.append(separador).append("idTaquillero=").append(idTaquillero);
            separador = "&";
        }
        redirect.append(separador).append("pagina=").append(Math.max(0, pagina));

        return redirect.toString();
    }

    /**
     * Muestra el panel de Facturas: tabla con todas las facturas,
     * ordenadas de más reciente a más antigua, con filtros por taquillero, estado y fecha.
     */
    @GetMapping("/facturas")
    public String mostrarPanelFacturas(
            @RequestParam(name = "idTaquillero", required = false) Long idTaquillero,
            @RequestParam(name = "estado", required = false) EstadoFactura estado,
            @RequestParam(name = "desde", required = false) LocalDate desde,
            @RequestParam(name = "hasta", required = false) LocalDate hasta,
            @RequestParam(name = "pagina", required = false, defaultValue = "0") int pagina,
            Model modelo) {

        // Cargamos todos los taquilleros (incluidos inactivos para historial)
        List<UsuarioResponse> todosLosTaquilleros = usuarioService.listarTaquilleros();
        modelo.addAttribute("taquilleros", todosLosTaquilleros);

        // Estado nulo = todos. EMITIDA = solo no anuladas.
        EstadoFactura estadoFiltro = (estado == null) ? EstadoFactura.EMITIDA : estado;

        // Rango de fechas: [desde 00:00, hasta + 1 día 00:00)
        LocalDateTime desdeHora = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime hastaHora = (hasta != null) ? hasta.plusDays(1).atStartOfDay() : null;

        PageRequest pageable = PageRequest.of(
                Math.max(0, pagina),
                TAMANO_PAGINA,
                Sort.by(Sort.Direction.DESC, "fechaHora"));

        Page<FacturaAdminResponse> paginaFacturas = ventaService.listarFacturasFiltradas(
                idTaquillero, estadoFiltro, desdeHora, hastaHora, pageable);

        modelo.addAttribute("paginaFacturas", paginaFacturas);
        modelo.addAttribute("paginaActual", paginaFacturas.getNumber());
        modelo.addAttribute("idTaquilleroSeleccionado", idTaquillero);
        modelo.addAttribute("estadoFiltro", estadoFiltro);
        modelo.addAttribute("desdeFiltro", desde);
        modelo.addAttribute("hastaFiltro", hasta);

        return "administracion/facturas";
    }

    /**
     * Muestra el modal de detalles de una factura existente para su visualizacion,
     * descarga e impresion (mismo comportamiento que tras generar una venta).
     */
    @GetMapping("/facturas/{id}/ver")
    public String verDetalleFactura(
            @PathVariable Long id,
            @RequestParam(name = "idTaquillero", required = false) Long idTaquillero,
            @RequestParam(name = "estado", required = false) EstadoFactura estado,
            @RequestParam(name = "desde", required = false) LocalDate desde,
            @RequestParam(name = "hasta", required = false) LocalDate hasta,
            @RequestParam(name = "pagina", required = false, defaultValue = "0") int pagina,
            @RequestParam(name = "ver", required = false, defaultValue = "false") boolean ver,
            Model modelo) {

        FacturaResponse factura = ventaService.buscarPorId(id);
        modelo.addAttribute("facturaEmitida", factura);
        modelo.addAttribute("verDetalle", ver);

        EstadoFactura estadoFiltro = (estado == null) ? EstadoFactura.EMITIDA : estado;
        LocalDateTime desdeHora = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime hastaHora = (hasta != null) ? hasta.plusDays(1).atStartOfDay() : null;

        PageRequest pageable = PageRequest.of(Math.max(0, pagina), TAMANO_PAGINA, Sort.by(Sort.Direction.DESC, "fechaHora"));
        Page<FacturaAdminResponse> paginaFacturas = ventaService.listarFacturasFiltradas(
                idTaquillero, estadoFiltro, desdeHora, hastaHora, pageable);

        List<UsuarioResponse> todosLosTaquilleros = usuarioService.listarTaquilleros();
        modelo.addAttribute("taquilleros", todosLosTaquilleros);
        modelo.addAttribute("paginaFacturas", paginaFacturas);
        modelo.addAttribute("paginaActual", paginaFacturas.getNumber());
        modelo.addAttribute("idTaquilleroSeleccionado", idTaquillero);
        modelo.addAttribute("estadoFiltro", estadoFiltro);
        modelo.addAttribute("desdeFiltro", desde);
        modelo.addAttribute("hastaFiltro", hasta);

        return "administracion/facturas";
    }
}

