package com.comfenalco.cine.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.PeliculaService;
import com.comfenalco.cine.dto.request.PeliculaRequest;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.enums.EstadoFuncion;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.Genero;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.PeliculaRepository;

@Service
@Transactional(readOnly = true)
public class PeliculaServiceImpl implements PeliculaService {

    private final PeliculaRepository peliculaRepository;
    private final FuncionRepository funcionRepository;

    public PeliculaServiceImpl(PeliculaRepository peliculaRepository, FuncionRepository funcionRepository) {
        this.peliculaRepository = peliculaRepository;
        this.funcionRepository = funcionRepository;
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
    public List<PeliculaResponse> listarPorGeneroYEstado(Genero genero, EstadoPelicula estado) {
        return peliculaRepository.findByGeneroAndEstado(genero, estado).stream()
                .map(PeliculaResponse::de)
                .toList();
    }

    @Override
    public List<PeliculaResponse> buscarPorTitulo(String titulo, EstadoPelicula estado) {
        String filtro = (titulo == null) ? "" : titulo.trim();
        return peliculaRepository.findByTituloContainingIgnoreCaseAndEstado(filtro, estado).stream()
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

        // AndIdNot: la película no se compara consigo misma
        if (peliculaRepository.existsByTituloIgnoreCaseAndIdNot(request.titulo(), id)) {
            throw new ReglaNegocioException("Ya existe otra película con ese título");
        }

        peliculaAEditar.setTitulo(request.titulo());
        peliculaAEditar.setSinopsis(request.sinopsis());
        peliculaAEditar.setDuracion(request.duracion());
        peliculaAEditar.setClasificacion(request.clasificacion());
        peliculaAEditar.setGenero(request.genero());
        peliculaAEditar.setImagenUrl(request.imagenUrl());

        return PeliculaResponse.de(peliculaAEditar); // sin save(): Hibernate guarda los cambios al terminar la transacción
    }

    @Override
    @Transactional 
    public void cambiarEstado(Long id, EstadoPelicula nuevoEstado) {

        Pelicula pelicula = peliculaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoEncontradoException("La película no existe"));

        if (pelicula.getEstado() == nuevoEstado) {
            return;   // nada que cambiar
        }

        if (nuevoEstado == EstadoPelicula.INACTIVA
                && funcionRepository.existsByPeliculaIdAndEstadoAndFechaGreaterThanEqual(
                        id, EstadoFuncion.PROGRAMADA, LocalDate.now())) {
            throw new ReglaNegocioException(
                "No se puede desactivar: la película tiene funciones programadas");
        }

        pelicula.setEstado(nuevoEstado);
    }

    private Pelicula obtenerEntidad(Long id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("La película no existe"));
    }

}