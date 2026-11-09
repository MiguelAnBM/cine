package com.comfenalco.cine.service;

import java.util.List;

import com.comfenalco.cine.dto.request.PeliculaRequest;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.Genero;

public interface PeliculaService {

    List<PeliculaResponse> listar();

    PeliculaResponse buscarPorId(Long id);

    List<PeliculaResponse> listarPorEstado(EstadoPelicula estado);

    List<PeliculaResponse> listarPorGeneroYEstado(Genero genero, EstadoPelicula estado);

    List<PeliculaResponse> buscarPorTitulo(String titulo, EstadoPelicula estado);

    PeliculaResponse crear(PeliculaRequest request);

    PeliculaResponse editar(Long id, PeliculaRequest request);

    void cambiarEstado(Long id, EstadoPelicula nuevoEstado);
}
