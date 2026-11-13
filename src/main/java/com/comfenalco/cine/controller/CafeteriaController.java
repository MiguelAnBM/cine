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

import com.comfenalco.cine.dto.request.PagoCafeteriaRequest;
import com.comfenalco.cine.dto.request.ProductoConfiteriaRequest;
import com.comfenalco.cine.dto.request.VentaRequest;
import com.comfenalco.cine.dto.response.CarritoResumenResponse;
import com.comfenalco.cine.dto.response.FacturaResponse;
import com.comfenalco.cine.dto.response.ProductoConfiteriaResponse;
import com.comfenalco.cine.model.enums.CategoriaProducto;
import com.comfenalco.cine.model.enums.EstadoProducto;
import com.comfenalco.cine.model.enums.MetodoPago;
import com.comfenalco.cine.service.CarritoService;
import com.comfenalco.cine.service.ProductoConfiteriaService;
import com.comfenalco.cine.service.UsuarioService;
import com.comfenalco.cine.service.VentaService;

import jakarta.validation.Valid;

/**
 * Controlador de Cafetería y Confitería.
 * Permite consultar el catálogo por categoría, delegar la gestión del carrito al CarritoService,
 * dar de alta nuevos productos y procesar el cobro de la venta.
 */
@Controller
@RequestMapping("/cafeteria")
public class CafeteriaController {

    private final ProductoConfiteriaService productoService;
    private final UsuarioService usuarioService;
    private final VentaService ventaService;
    private final CarritoService carritoService;

    public CafeteriaController(ProductoConfiteriaService productoService,
                               UsuarioService usuarioService,
                               VentaService ventaService,
                               CarritoService carritoService) {
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.ventaService = ventaService;
        this.carritoService = carritoService;
    }

    /**
     * Muestra el catálogo de productos y el pedido actual del carrito.
     */
    @GetMapping
    public String mostrarCafeteria(
            @RequestParam(name = "categoria", required = false) CategoriaProducto categoriaSeleccionada,
            Model modelo) {

        // 1. Obtenemos los productos (activos e inactivos) delegando la verificación de categoría nula al servicio
        List<ProductoConfiteriaResponse> listaDeProductos =
                productoService.listarPorCategoria(categoriaSeleccionada);

        // 2. Obtenemos el resumen del carrito directamente desde el servicio de sesión
        CarritoResumenResponse resumenCarrito = carritoService.obtenerResumen();

        // 3. Enviamos al modelo los datos necesarios para la vista
        modelo.addAttribute("productos", listaDeProductos);
        modelo.addAttribute("categorias", CategoriaProducto.values());
        modelo.addAttribute("renglonesCarrito", resumenCarrito.lineas());
        modelo.addAttribute("totalPedido", resumenCarrito.total());
        modelo.addAttribute("taquillerosActivos", usuarioService.listarTaquillerosActivos());
        modelo.addAttribute("metodosDePago", MetodoPago.values());

        if (!modelo.containsAttribute("pagoFormulario")) {
            modelo.addAttribute("pagoFormulario", new PagoCafeteriaRequest(null, null, null));
        }

        return "cafeteria/index";
    }

    /**
     * Agrega un producto al carrito delegando en el CarritoService.
     */
    @PostMapping("/carrito/agregar")
    public String agregarProductoAlCarrito(@RequestParam(name = "idProducto") Long idProducto) {
        carritoService.agregar(idProducto);
        return "redirect:/cafeteria";
    }

    /**
     * Resta o remueve un producto del carrito.
     */
    @PostMapping("/carrito/quitar")
    public String quitarProductoDelCarrito(@RequestParam(name = "idProducto") Long idProducto) {
        carritoService.quitar(idProducto);
        return "redirect:/cafeteria";
    }

    /**
     * Vacía completamente los productos del carrito.
     */
    @PostMapping("/carrito/vaciar")
    public String vaciarCarrito() {
        carritoService.vaciar();
        return "redirect:/cafeteria";
    }

    /**
     * Procesa el cobro de los productos acumulados en el carrito.
     */
    @PostMapping("/pagar")
    public String pagarPedido(
            @Valid @ModelAttribute("pagoFormulario") PagoCafeteriaRequest pago,
            BindingResult resultadoValidacion,
            @RequestParam(name = "idTaquillero") Long idTaquillero,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            cargarDatosBase(modelo);
            return "cafeteria/index";
        }

        VentaRequest solicitudDeVenta = new VentaRequest(
                null, // Venta pura de cafetería (sin función de cine)
                null, // idTaquillero: se pasa aparte en ventaService.vender()
                null,
                pago.metodoPago(),
                pago.nombreCliente(),
                pago.correoCliente(),
                carritoService.obtenerItemsParaVenta()
        );

        FacturaResponse factura = ventaService.vender(idTaquillero, solicitudDeVenta);

        // Vaciamos el carrito tras el cobro exitoso
        carritoService.vaciar();

        mensajesFlash.addFlashAttribute("facturaEmitida", factura);

        return "redirect:/cafeteria";
    }

    /**
     * Muestra el formulario para crear un nuevo producto de cafetería.
     */
    @GetMapping("/productos/nuevo")
    public String mostrarFormularioCrearProducto(Model modelo) {
        if (!modelo.containsAttribute("productoFormulario")) {
            modelo.addAttribute("productoFormulario",
                    new ProductoConfiteriaRequest("", "", null, "", CategoriaProducto.SNACKS));
        }
        modelo.addAttribute("categorias", CategoriaProducto.values());
        modelo.addAttribute("esEdicion", false);
        return "cafeteria/producto-form";
    }

    /**
     * Muestra el formulario precargado con los datos de un producto existente para editarlo.
     */
    @GetMapping("/productos/{idProducto}/editar")
    public String mostrarFormularioEditarProducto(@PathVariable Long idProducto, Model modelo) {
        ProductoConfiteriaResponse productoExistente = productoService.buscarPorId(idProducto);

        ProductoConfiteriaRequest productoFormulario = new ProductoConfiteriaRequest(
                productoExistente.nombre(),
                productoExistente.descripcion(),
                productoExistente.precio(),
                productoExistente.imagenUrl(),
                productoExistente.categoria()
        );

        modelo.addAttribute("productoFormulario", productoFormulario);
        modelo.addAttribute("idProducto", idProducto);
        modelo.addAttribute("esEdicion", true);
        modelo.addAttribute("categorias", CategoriaProducto.values());

        return "cafeteria/producto-form";
    }

    /**
     * Guarda el nuevo producto creado.
     */
    @PostMapping("/productos")
    public String guardarNuevoProducto(
            @Valid @ModelAttribute("productoFormulario") ProductoConfiteriaRequest datosProducto,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("categorias", CategoriaProducto.values());
            modelo.addAttribute("esEdicion", false);
            return "cafeteria/producto-form";
        }

        productoService.crear(datosProducto);
        mensajesFlash.addFlashAttribute("exito", "Producto «" + datosProducto.nombre() + "» agregado a cafetería.");
        return "redirect:/cafeteria";
    }

    /**
     * Procesa la actualización de los datos de un producto de cafetería.
     */
    @PostMapping("/productos/{idProducto}")
    public String actualizarProducto(
            @PathVariable Long idProducto,
            @Valid @ModelAttribute("productoFormulario") ProductoConfiteriaRequest datosActualizados,
            BindingResult resultadoValidacion,
            Model modelo,
            RedirectAttributes mensajesFlash) {

        if (resultadoValidacion.hasErrors()) {
            modelo.addAttribute("idProducto", idProducto);
            modelo.addAttribute("esEdicion", true);
            modelo.addAttribute("categorias", CategoriaProducto.values());
            return "cafeteria/producto-form";
        }

        productoService.editar(idProducto, datosActualizados);
        mensajesFlash.addFlashAttribute("exito", "Producto «" + datosActualizados.nombre() + "» actualizado correctamente.");
        return "redirect:/cafeteria";
    }

    /**
     * Activa o desactiva lógicamente un producto de cafetería según el parámetro recibido.
     */
    @PostMapping("/productos/{idProducto}/estado")
    public String cambiarEstadoProducto(
            @PathVariable Long idProducto,
            @RequestParam(name = "nuevoEstado") EstadoProducto nuevoEstado,
            RedirectAttributes mensajesFlash) {

        productoService.cambiarEstado(idProducto, nuevoEstado);

        String mensajeEstado = (nuevoEstado == EstadoProducto.ACTIVO)
                ? "El producto fue reactivado en la cafetería."
                : "El producto fue desactivado de la cafetería.";

        mensajesFlash.addFlashAttribute("exito", mensajeEstado);
        return "redirect:/cafeteria";
    }

    /**
     * Carga los datos base que necesita la vista de cafetería cuando hay que re-renderizarla
     * tras un error de validación (productos, carrito, taquilleros y métodos de pago).
     */
    private void cargarDatosBase(Model modelo) {
        List<ProductoConfiteriaResponse> listaDeProductos =
                productoService.listarPorCategoria(null);

        CarritoResumenResponse resumenCarrito = carritoService.obtenerResumen();

        modelo.addAttribute("productos", listaDeProductos);
        modelo.addAttribute("categorias", CategoriaProducto.values());
        modelo.addAttribute("renglonesCarrito", resumenCarrito.lineas());
        modelo.addAttribute("totalPedido", resumenCarrito.total());
        modelo.addAttribute("taquillerosActivos", usuarioService.listarTaquillerosActivos());
        modelo.addAttribute("metodosDePago", MetodoPago.values());
    }
}
