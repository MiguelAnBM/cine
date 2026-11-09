package com.comfenalco.cine.service;

import java.util.List;

import com.comfenalco.cine.dto.request.CambiarContrasenaRequest;
import com.comfenalco.cine.dto.request.UsuarioCrearRequest;
import com.comfenalco.cine.dto.request.UsuarioEditarRequest;
import com.comfenalco.cine.dto.response.UsuarioResponse;
import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.EstadoUsuario;

public interface UsuarioService {

    List<UsuarioResponse> listar();

    UsuarioResponse buscarPorId(Long id);

    UsuarioResponse buscarPorUsername(String username);

    List<UsuarioResponse> listarPorEstado(EstadoUsuario estado);

    UsuarioResponse crear(UsuarioCrearRequest request);

    UsuarioResponse editar(Long id, UsuarioEditarRequest request);

    void cambiarContrasena(Long id, CambiarContrasenaRequest request);

    void cambiarEstado(Long id, EstadoUsuario nuevoEstado);

    // Solo para otros servicios (por ejemplo, ventas). No devolver esta entidad desde un controlador
    Usuario obtenerEntidad(Long id);
}