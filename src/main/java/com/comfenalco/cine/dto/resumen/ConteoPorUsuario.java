package com.comfenalco.cine.dto.resumen;

// Fila interna de una consulta: facturas emitidas por un usuario
public record ConteoPorUsuario(Long idUsuario, String nombre, Long facturas) {
}