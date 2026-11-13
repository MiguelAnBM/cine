package com.comfenalco.cine.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.comfenalco.cine.dto.request.ItemConfiteriaRequest;
import com.comfenalco.cine.dto.response.CarritoResumenResponse;
import com.comfenalco.cine.dto.response.ProductoConfiteriaResponse;
import com.comfenalco.cine.dto.response.RenglonCarritoResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.model.enums.EstadoProducto;
import com.comfenalco.cine.service.CarritoService;
import com.comfenalco.cine.service.ProductoConfiteriaService;

/**
 * Implementación del carrito de cafetería con alcance de sesión (@SessionScope).
 * Spring crea automáticamente una instancia independiente para cada usuario conectado,
 * aislando los pedidos de cada cliente y liberando al controlador de manejar sesiones HTTP.
 */
@Service
@SessionScope
public class CarritoServiceImpl implements CarritoService {

    // Guarda los IDs de producto y sus cantidades para la sesión actual
    private final Map<Long, Integer> itemsDelCarrito = new LinkedHashMap<>();

    private final ProductoConfiteriaService productoConfiteriaService;

    public CarritoServiceImpl(ProductoConfiteriaService productoConfiteriaService) {
        this.productoConfiteriaService = productoConfiteriaService;
    }

    @Override
    public void agregar(Long idProducto) {
        if (idProducto == null || buscarProductoActivo(idProducto) == null) {
            return;
        }
        itemsDelCarrito.merge(idProducto, 1, Integer::sum);
    }

    @Override
    public void quitar(Long idProducto) {
        if (idProducto == null || !itemsDelCarrito.containsKey(idProducto)) {
            return;
        }
        int cantidadActual = itemsDelCarrito.get(idProducto);
        if (cantidadActual > 1) {
            itemsDelCarrito.put(idProducto, cantidadActual - 1);
        } else {
            itemsDelCarrito.remove(idProducto);
        }
    }

    @Override
    public void vaciar() {
        itemsDelCarrito.clear();
    }

    @Override
    public CarritoResumenResponse obtenerResumen() {

        // Snapshot de IDs para hacer una sola consulta batch en lugar de N consultas (una por producto)
        List<Long> idsSnapshot = new ArrayList<>(itemsDelCarrito.keySet());

        // Una sola consulta: productos activos cuyos IDs están en el carrito
        Map<Long, ProductoConfiteriaResponse> productosActivos =
                productoConfiteriaService.buscarActivosPorIds(idsSnapshot);

        List<RenglonCarritoResponse> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Iterator<Map.Entry<Long, Integer>> iterator = itemsDelCarrito.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> entrada = iterator.next();
            Long idProducto = entrada.getKey();

            // Si el producto ya no existe o está inactivo, se elimina del carrito
            ProductoConfiteriaResponse producto = productosActivos.get(idProducto);
            if (producto == null) {
                iterator.remove();
                continue;
            }

            BigDecimal subtotal = producto.precio().multiply(BigDecimal.valueOf(entrada.getValue()));
            lineas.add(new RenglonCarritoResponse(
                    producto.id(),
                    producto.nombre(),
                    producto.precio(),
                    entrada.getValue(),
                    subtotal
            ));
            total = total.add(subtotal);
        }

        return new CarritoResumenResponse(lineas, total);
    }

    @Override
    public List<ItemConfiteriaRequest> obtenerItemsParaVenta() {

        // Snapshot de IDs para hacer una sola consulta batch
        List<Long> idsSnapshot = new ArrayList<>(itemsDelCarrito.keySet());

        // Una sola consulta: productos activos cuyos IDs están en el carrito
        Map<Long, ProductoConfiteriaResponse> productosActivos =
                productoConfiteriaService.buscarActivosPorIds(idsSnapshot);

        List<ItemConfiteriaRequest> itemsParaVenta = new ArrayList<>();
        Iterator<Map.Entry<Long, Integer>> iterator = itemsDelCarrito.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> entrada = iterator.next();
            Long idProducto = entrada.getKey();

            // Si el producto ya no existe o está inactivo, se elimina del carrito
            if (!productosActivos.containsKey(idProducto)) {
                iterator.remove();
                continue;
            }

            itemsParaVenta.add(new ItemConfiteriaRequest(entrada.getKey(), entrada.getValue()));
        }

        return itemsParaVenta;
    }

    // Busca un producto individual por ID (se mantiene para agregar al carrito uno a uno)
    private ProductoConfiteriaResponse buscarProductoActivo(Long idProducto) {
        try {
            ProductoConfiteriaResponse producto = productoConfiteriaService.buscarPorId(idProducto);
            return producto != null && producto.estado() == EstadoProducto.ACTIVO ? producto : null;
        } catch (NoEncontradoException e) {
            return null;
        }
    }
}
