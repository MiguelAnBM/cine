package com.comfenalco.cine.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.comfenalco.cine.dto.response.AsientoResponse;

public interface DetalleBoletoService {

    // Los 80 asientos de la función, en orden A1..H10, marcados como ocupados o libres
    List<AsientoResponse> mapaDeAsientos(Long idFuncion);

    // Lanza ReglaNegocioException si algún asiento no existe, está repetido o ya fue vendido
    void validarDisponibles(Long idFuncion, Collection<String> codigosAsiento);

    // Para bloquear cambios de sala, fecha u hora en una función con boletos vendidos
    boolean tieneBoletosVendidos(Long idFuncion);

    // Boletos vigentes (no anulados) de una función
    long contarBoletos(Long idFuncion);

    // Boletos vigentes de varias funciones en una sola consulta. Las que no tienen boletos no aparecen en el mapa
    Map<Long, Long> contarBoletosPorFuncion(Collection<Long> idsFuncion);
}
