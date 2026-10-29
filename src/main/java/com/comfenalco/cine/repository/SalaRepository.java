package com.comfenalco.cine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.enums.EstadoSala;

public interface SalaRepository extends JpaRepository<Sala, Long> {
    
    boolean existsByNombre(String nombre); // al crear

    boolean existsByNombreAndIdNot(String nombre, Long id);  // al editar: ignora la propia sala
    
    List<Sala> findByEstado(EstadoSala estado);
}
