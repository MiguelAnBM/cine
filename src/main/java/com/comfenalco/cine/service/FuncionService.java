package com.comfenalco.cine.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.comfenalco.cine.dto.request.FuncionRequest;
import com.comfenalco.cine.dto.response.FuncionResponse;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.model.enums.EstadoFuncion;

public interface FuncionService {

    List<FuncionResponse> cartelera(LocalDate fecha);

    // Todas las funciones, de la más reciente a la más antigua (pantalla Funciones)
    Page<FuncionResponse> listar(Pageable pageable);

    // Las próximas funciones programadas que aún no empiezan (pantalla Inicio)
    List<FuncionResponse> proximas(int limite);

    List<FuncionResponse> proximasPorPelicula(Long idPelicula);

    List<PeliculaResponse> peliculasEnCartelera();

    FuncionResponse buscarPorId(Long id);

    FuncionResponse crear(FuncionRequest request);

    FuncionResponse editar(Long id, FuncionRequest request);

    // Devuelve cuántas facturas tienen boletos de esta función (para avisar de devoluciones)
    long cancelar(Long id);

    // Para EN_CURSO y FINALIZADA. Para cancelar se usa cancelar()
    void cambiarEstado(Long id, EstadoFuncion nuevoEstado);
}