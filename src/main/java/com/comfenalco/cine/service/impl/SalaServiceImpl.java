package com.comfenalco.cine.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.SalaService;
import com.comfenalco.cine.service.FuncionVendibilidad;
import com.comfenalco.cine.dto.request.SalaRequest;
import com.comfenalco.cine.dto.response.SalaResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.SalaRepository;

@Service
@Transactional(readOnly = true)
public class SalaServiceImpl implements SalaService {

    private final SalaRepository salaRepository;
    private final FuncionRepository funcionRepository;
    private final Clock reloj;

    public SalaServiceImpl(SalaRepository salaRepository, FuncionRepository funcionRepository, Clock reloj) {
        this.salaRepository = salaRepository;
        this.funcionRepository = funcionRepository;
        this.reloj = reloj;
    }

    @Override
    public List<SalaResponse> listar() {
        return salaRepository.findAll().stream()
                .map(SalaResponse::de)
                .toList();
    }

    @Override
    public SalaResponse buscarPorId(Long id) {
        return SalaResponse.de(obtenerEntidad(id));
    }

    @Override
    public List<SalaResponse> listarPorEstado(EstadoSala estado) {
        return salaRepository.findByEstadoOrderByNombre(estado).stream()
                .map(SalaResponse::de)
                .toList();
    }

    @Override
    @Transactional
    public SalaResponse crear(SalaRequest request) {

        if (salaRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new ReglaNegocioException("Ya existe una sala con ese nombre");
        }

        Sala sala = new Sala(request.nombre(), request.tipo(), request.precioBoleto());

        return SalaResponse.de(salaRepository.save(sala));
    }

    @Override
    @Transactional
    public SalaResponse editar(Long id, SalaRequest request) {

        Sala sala = obtenerEntidad(id);

        if (salaRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)) {
            throw new ReglaNegocioException("Ya existe otra sala con ese nombre");
        }

        sala.setNombre(request.nombre());
        sala.setTipo(request.tipo());
        sala.setPrecioBoleto(request.precioBoleto());

        return SalaResponse.de(sala);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoSala nuevoEstado) {

        Sala sala = salaRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new NoEncontradoException("La sala no existe"));

        if (sala.getEstado() == nuevoEstado) {
            return;
        }

        if (nuevoEstado == EstadoSala.INACTIVA) {
            LocalDateTime ahora = LocalDateTime.now(reloj);
            boolean tieneFuncionVigente = funcionRepository.findBySalaIdAndFechaGreaterThanEqual(
                    id, ahora.toLocalDate()).stream()
                    .anyMatch(f -> !FuncionVendibilidad.haFinalizado(
                            LocalDateTime.of(f.getFecha(), f.getHoraInicio()),
                            f.getPelicula().duracionEnMinutos(),
                            ahora));
            if (tieneFuncionVigente) {
                throw new ReglaNegocioException(
                        "No se puede desactivar: la sala tiene funciones vigentes");
            }
        }

        sala.setEstado(nuevoEstado);
    }

    private Sala obtenerEntidad(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("La sala no existe"));
    }
}
