package com.comfenalco.cine.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.PeliculaService;
import com.comfenalco.cine.service.FuncionVendibilidad;
import com.comfenalco.cine.dto.request.PeliculaRequest;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.PeliculaRepository;

@Service
@Transactional(readOnly = true)
public class PeliculaServiceImpl implements PeliculaService {

    private final PeliculaRepository peliculaRepository;
    private final FuncionRepository funcionRepository;
    private final Clock reloj;

    public PeliculaServiceImpl(PeliculaRepository peliculaRepository, FuncionRepository funcionRepository, Clock reloj) {
        this.peliculaRepository = peliculaRepository;
        this.funcionRepository = funcionRepository;
        this.reloj = reloj;
    }

    @Override
    public List<PeliculaResponse> listar() {
        return peliculaRepository.findAll().stream()
                .map(PeliculaResponse::de)
                .toList();
    }

    @Override
    public PeliculaResponse buscarPorId(Long id) {
        return PeliculaResponse.de(obtenerEntidad(id));
    }

    @Override
    public List<PeliculaResponse> listarPorEstado(EstadoPelicula estado) {
        return peliculaRepository.findByEstado(estado).stream()
                .map(PeliculaResponse::de)
                .toList();
    }

    @Override
    @Transactional
    public PeliculaResponse crear(PeliculaRequest request) {

        if (peliculaRepository.existsByTituloIgnoreCase(request.titulo())) {
            throw new ReglaNegocioException("Ya existe una película con ese título");
        }

        Pelicula pelicula = new Pelicula(
                request.titulo(),
                request.sinopsis(),
                request.duracion(),
                request.clasificacion(),
                request.genero(),
                request.imagenUrl());

        return PeliculaResponse.de(peliculaRepository.save(pelicula));
    }

    @Override
    @Transactional
    public PeliculaResponse editar(Long id, PeliculaRequest request) {

        Pelicula peliculaAEditar = obtenerEntidad(id);

        if (peliculaRepository.existsByTituloIgnoreCaseAndIdNot(request.titulo(), id)) {
            throw new ReglaNegocioException("Ya existe otra película con ese título");
        }

        peliculaAEditar.setTitulo(request.titulo());
        peliculaAEditar.setSinopsis(request.sinopsis());
        peliculaAEditar.setDuracion(request.duracion());
        peliculaAEditar.setClasificacion(request.clasificacion());
        peliculaAEditar.setGenero(request.genero());
        peliculaAEditar.setImagenUrl(request.imagenUrl());

        return PeliculaResponse.de(peliculaAEditar);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoPelicula nuevoEstado) {

        Pelicula pelicula = peliculaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoEncontradoException("La película no existe"));

        if (pelicula.getEstado() == nuevoEstado) {
            return;
        }

        if (nuevoEstado == EstadoPelicula.INACTIVA) {
            LocalDateTime ahora = LocalDateTime.now(reloj);
            boolean tieneFuncionVigente = funcionRepository.findByPeliculaIdAndFechaGreaterThanEqual(
                    id, ahora.toLocalDate()).stream()
                    .anyMatch(f -> !FuncionVendibilidad.haFinalizado(
                            LocalDateTime.of(f.getFecha(), f.getHoraInicio()),
                            f.getPelicula().duracionEnMinutos(),
                            ahora));
            if (tieneFuncionVigente) {
                throw new ReglaNegocioException(
                        "No se puede desactivar: la película tiene funciones vigentes");
            }
        }

        pelicula.setEstado(nuevoEstado);
    }

    private Pelicula obtenerEntidad(Long id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("La película no existe"));
    }

}
