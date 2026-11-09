package com.comfenalco.cine.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.comfenalco.cine.exception.ReglaNegocioException;

public final class ValidadorRango {

    public static final int MAX_DIAS_CONSULTA = 366;

    private ValidadorRango() {
    }

    public static void validar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new ReglaNegocioException("El rango de fechas no es válido");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) >= MAX_DIAS_CONSULTA) {
            throw new ReglaNegocioException("El rango máximo es de " + MAX_DIAS_CONSULTA + " días");
        }
    }
}
