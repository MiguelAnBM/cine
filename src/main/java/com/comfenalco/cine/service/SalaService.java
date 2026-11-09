package com.comfenalco.cine.service;

import java.util.List;

import com.comfenalco.cine.dto.request.SalaRequest;
import com.comfenalco.cine.dto.response.SalaResponse;
import com.comfenalco.cine.model.enums.EstadoSala;

public interface SalaService {

    List<SalaResponse> listar();

    SalaResponse buscarPorId(Long id);

    List<SalaResponse> listarPorEstado(EstadoSala estado);

    SalaResponse crear(SalaRequest request);

    SalaResponse editar(Long id, SalaRequest request);

    void cambiarEstado(Long id, EstadoSala nuevoEstado);
}
