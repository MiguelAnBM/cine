package com.comfenalco.cine.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.UsuarioService;
import com.comfenalco.cine.dto.request.UsuarioCrearRequest;
import com.comfenalco.cine.dto.request.UsuarioEditarRequest;
import com.comfenalco.cine.dto.response.UsuarioResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.EstadoUsuario;
import com.comfenalco.cine.model.enums.RolUsuario;
import com.comfenalco.cine.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<UsuarioResponse> listarTaquilleros() {
        // Obtenemos todos los usuarios cuyo rol sea TAQUILLERO (tanto activos como inactivos)
        List<Usuario> listaDeTaquilleros = usuarioRepository
                .findByRolOrderByNombreCompleto(RolUsuario.TAQUILLERO);

        return listaDeTaquilleros.stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Override
    public List<UsuarioResponse> listarTaquillerosActivos() {
        // Obtenemos únicamente los taquilleros que se encuentren en estado ACTIVO (para ventas)
        List<Usuario> listaDeTaquillerosActivos = usuarioRepository
                .findByRolAndEstadoOrderByNombreCompleto(RolUsuario.TAQUILLERO, EstadoUsuario.ACTIVO);

        return listaDeTaquillerosActivos.stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Override
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.de(obtenerEntidad(id));
    }

    @Override
    public UsuarioResponse buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameAndEstado(username, EstadoUsuario.ACTIVO)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new NoEncontradoException("El usuario no existe"));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {

        // Siempre se crea un taquillero: sin username y sin contraseña
        Usuario usuario = new Usuario(
                request.nombreCompleto(),
                "", // username vacío: no se usa para taquilleros
                RolUsuario.TAQUILLERO,
                null);

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse editar(Long id, UsuarioEditarRequest request) {

        Usuario usuario = obtenerEntidad(id);

        // El rol no se puede cambiar
        usuario.setNombreCompleto(request.nombreCompleto());

        return UsuarioResponse.de(usuario);   // sin save(): Hibernate guarda al terminar la transacción
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoUsuario nuevoEstado) {

        Usuario usuario = obtenerEntidad(id);

        if (usuario.getEstado() == nuevoEstado) {
            return;   // nada que cambiar
        }

        if (nuevoEstado == EstadoUsuario.INACTIVO
                && usuario.getRol() == RolUsuario.ADMINISTRADOR) {

            // Primero se bloquean todos los administradores activos: dos bajas
            // simultáneas de administradores distintos no pueden cruzarse
            List<Usuario> administradoresActivos = usuarioRepository.findByRolAndEstadoForUpdate(
                    RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO);

            Usuario objetivo = administradoresActivos.stream()
                    .filter(admin -> admin.getId().equals(id))
                    .findFirst()
                    .orElseGet(() -> usuarioRepository.findByIdForUpdate(id)
                            .orElseThrow(() -> new NoEncontradoException("El usuario no existe")));

            if (objetivo.getEstado() == nuevoEstado) {
                return;
            }

            if (administradoresActivos.size() <= 1) {
                throw new ReglaNegocioException("No se puede desactivar al único administrador activo");
            }

            objetivo.setEstado(nuevoEstado);
            return;
        }

        Usuario bloqueado = usuarioRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoEncontradoException("El usuario no existe"));

        if (bloqueado.getEstado() == nuevoEstado) {
            return;
        }

        bloqueado.setEstado(nuevoEstado);
    }

    @Override
    public Usuario obtenerEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("El usuario no existe"));
    }
}
