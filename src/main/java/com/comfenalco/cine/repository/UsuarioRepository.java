package com.comfenalco.cine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.EstadoUsuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    Optional<Usuario> findByUsernameAndEstado(String username, EstadoUsuario estado);  // login: solo activos
    
    List<Usuario> findByEstado(EstadoUsuario estado);

}
