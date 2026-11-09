package com.comfenalco.cine.dto.resumen;

// Fila interna de una consulta: boletos vigentes de una función
public record ConteoPorFuncion(Long idFuncion, Long boletos) {
}