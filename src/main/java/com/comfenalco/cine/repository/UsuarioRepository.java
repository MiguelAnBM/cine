package com.comfenalco.cine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.EstadoUsuario;
import com.comfenalco.cine.model.enums.RolUsuario;

import jakarta.persistence.LockModeType;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Evita desactivar dos administradores a la vez y quedarse sin ninguno activo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);

    // Bloquea la lista completa para que dos bajas de administradores no se crucen
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.estado = :estado")
    List<Usuario> findByRolAndEstadoForUpdate(@Param("rol") RolUsuario rol,
                                              @Param("estado") EstadoUsuario estado);

    Optional<Usuario> findByUsernameAndEstado(String username, EstadoUsuario estado);  // login: solo activos

    // Métodos para consultar taquilleros (todos o solo los activos)
    List<Usuario> findByRolOrderByNombreCompleto(RolUsuario rol);

    List<Usuario> findByRolAndEstadoOrderByNombreCompleto(RolUsuario rol, EstadoUsuario estado);
}
