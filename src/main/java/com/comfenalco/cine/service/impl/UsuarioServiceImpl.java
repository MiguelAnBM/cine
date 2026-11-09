package com.comfenalco.cine.service.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.UsuarioService;
import com.comfenalco.cine.dto.request.CambiarContrasenaRequest;
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
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("nombreCompleto")).stream()
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
    public List<UsuarioResponse> listarPorEstado(EstadoUsuario estado) {
        return usuarioRepository.findByEstadoOrderByNombreCompleto(estado).stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {

        if (usuarioRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ReglaNegocioException("Ya existe un usuario con ese nombre de usuario");
        }

        String hash = null;

        if (request.rol() == RolUsuario.ADMINISTRADOR) {
            if (request.contrasena() == null) {
                throw new ReglaNegocioException("El administrador debe tener contraseña");
            }
            hash = passwordEncoder.encode(request.contrasena());

        } else if (request.contrasena() != null) {
            throw new ReglaNegocioException("El taquillero no usa contraseña");
        }

        Usuario usuario = new Usuario(
                request.nombreCompleto(),
                request.username(),
                request.rol(),
                hash);

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse editar(Long id, UsuarioEditarRequest request) {

        Usuario usuario = obtenerEntidad(id);

        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), id)) {
            throw new ReglaNegocioException("Ya existe otro usuario con ese nombre de usuario");
        }

        // El rol no se puede cambiar
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setUsername(request.username());

        return UsuarioResponse.de(usuario);   // sin save(): Hibernate guarda al terminar la transacción
    }

    @Override
    @Transactional
    public void cambiarContrasena(Long id, CambiarContrasenaRequest request) {

        Usuario usuario = obtenerEntidad(id);

        if (usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new ReglaNegocioException("Solo los administradores tienen contraseña");
        }

        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasenaHash())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(request.contrasenaNueva()));
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