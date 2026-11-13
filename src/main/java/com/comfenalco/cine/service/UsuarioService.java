package com.comfenalco.cine.service;

import java.util.List;

import com.comfenalco.cine.dto.request.UsuarioCrearRequest;
import com.comfenalco.cine.dto.request.UsuarioEditarRequest;
import com.comfenalco.cine.dto.response.UsuarioResponse;
import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.EstadoUsuario;

public interface UsuarioService {

    // Consultas específicas para taquilleros
    List<UsuarioResponse> listarTaquilleros();

    List<UsuarioResponse> listarTaquillerosActivos();

    UsuarioResponse buscarPorId(Long id);

    UsuarioResponse buscarPorUsername(String username);

    UsuarioResponse crear(UsuarioCrearRequest request);

    UsuarioResponse editar(Long id, UsuarioEditarRequest request);

    void cambiarEstado(Long id, EstadoUsuario nuevoEstado);

    // Solo para otros servicios (por ejemplo, ventas). No devolver esta entidad desde un controlador
    Usuario obtenerEntidad(Long id);
}
