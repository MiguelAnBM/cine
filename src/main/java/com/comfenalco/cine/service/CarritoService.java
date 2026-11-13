package com.comfenalco.cine.service;

import java.util.List;

import com.comfenalco.cine.dto.request.ItemConfiteriaRequest;
import com.comfenalco.cine.dto.response.CarritoResumenResponse;

/**
 * Servicio encargado de gestionar el carrito de compras de cafetería.
 * Mantiene los productos agregados por cada cliente durante su sesión
 * y prepara los datos para la vista y para el cobro final.
 */
public interface CarritoService {

    /**
     * Agrega una unidad de un producto al carrito de la sesión.
     */
    void agregar(Long idProducto);

    /**
     * Resta una unidad de un producto del carrito, o lo elimina si llega a cero.
     */
    void quitar(Long idProducto);

    /**
     * Vacía todos los productos del carrito.
     */
    void vaciar();

    /**
     * Calcula los subtotales de cada producto y el total global del pedido.
     */
    CarritoResumenResponse obtenerResumen();

    /**
     * Transforma el carrito al formato de DTOs que requiere VentaService para generar la factura.
     */
    List<ItemConfiteriaRequest> obtenerItemsParaVenta();
}

