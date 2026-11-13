package com.comfenalco.cine.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.comfenalco.cine.dto.response.FacturaResponse;
import com.comfenalco.cine.service.VentaService;

/**
 * Controlador para la visualización, impresión y descarga de comprobantes de factura.
 */
@Controller
@RequestMapping("/facturas")
public class FacturaController {

    private final VentaService ventaService;

    public FacturaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Muestra la vista imprimible limpia y sin estilos de la aplicación, optimizada
     * para envío directo a la impresora térmica o estándar.
     */
    @GetMapping("/{id}/imprimir")
    public String imprimirFactura(@PathVariable Long id, Model modelo) {
        FacturaResponse factura = ventaService.buscarPorId(id);
        modelo.addAttribute("factura", factura);
        return "facturas/imprimir";
    }

    /**
     * Descarga un archivo de texto plano (.txt) con los datos estructurados de la factura.
     */
    @GetMapping("/{id}/descargar-txt")
    public ResponseEntity<byte[]> descargarTxt(@PathVariable Long id) {
        FacturaResponse factura = ventaService.buscarPorId(id);
        byte[] contenido = ventaService.generarTextoFactura(factura).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura-" + id + ".txt\"")
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(contenido);
    }
}

