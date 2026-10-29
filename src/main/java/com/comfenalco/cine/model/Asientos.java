package com.comfenalco.cine.model;

import java.util.ArrayList;
import java.util.List;

// No es una entidad
public final class Asientos {

    public static final int FILAS = 8;       // A-H
    public static final int COLUMNAS = 10;   // 1-10

    private Asientos() {
        
    }

    public static List<String> todos() {
        List<String> codigos = new ArrayList<>();
        for (int f = 0; f < FILAS; f++) {
            for (int c = 1; c <= COLUMNAS; c++) {
                codigos.add("" + (char) ('A' + f) + c);
            }
        }
        return codigos;
    }
}
