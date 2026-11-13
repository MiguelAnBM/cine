package com.comfenalco.cine.service;

import java.time.LocalDateTime;

public final class FuncionVendibilidad {

    public static final int MINUTOS_VENTA_TRAS_INICIO = 20;

    private FuncionVendibilidad() {
    }

    public static boolean haFinalizado(LocalDateTime inicio, int duracionMinutos, LocalDateTime ahora) {
        return !inicio.plusMinutes(duracionMinutos).isAfter(ahora);
    }

    public static boolean esVendible(LocalDateTime inicio, int duracionMinutos, LocalDateTime ahora) {
        return ahora.isBefore(inicio.plusMinutes(MINUTOS_VENTA_TRAS_INICIO))
                && !haFinalizado(inicio, duracionMinutos, ahora);
    }
}
