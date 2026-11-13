package com.comfenalco.cine.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.comfenalco.cine.dto.request.FuncionRequest;
import com.comfenalco.cine.dto.response.FuncionResponse;

public interface FuncionService {

    // Todas las funciones, de la más reciente a la más antigua (pantalla Funciones)
    Page<FuncionResponse> listar(Pageable pageable);

    // Las próximas funciones programadas que aún no empiezan
    List<FuncionResponse> proximas(int limite);

    // Las próximas funciones programadas del día actual que aún no empiezan (pantalla Inicio)
    List<FuncionResponse> proximasDeHoy(int limite);

    FuncionResponse buscarPorId(Long id);

    FuncionResponse crear(FuncionRequest request);

    FuncionResponse editar(Long id, FuncionRequest request);

    // Valida que la función sea vendible antes de mostrar el mapa de asientos
    void validarVendible(Long idFuncion);

    // Cuenta cuántas funciones activas o programadas hay para una fecha dada
    long contarFuncionesDelDia(LocalDate fecha);
}
