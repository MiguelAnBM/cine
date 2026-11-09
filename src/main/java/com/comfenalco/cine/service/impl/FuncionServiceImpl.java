package com.comfenalco.cine.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.DetalleBoletoService;
import com.comfenalco.cine.service.FuncionService;
import com.comfenalco.cine.dto.request.FuncionRequest;
import com.comfenalco.cine.dto.response.FuncionResponse;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.Funcion;
import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.enums.EstadoFuncion;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.PeliculaRepository;
import com.comfenalco.cine.repository.SalaRepository;

@Service
@Transactional(readOnly = true)
public class FuncionServiceImpl implements FuncionService {

    // Tiempo de limpieza entre funciones de la misma sala
    private static final int MARGEN_MINUTOS = 15;

    private static final int MAX_TAMANO_PAGINA = 100;
    private static final int MAX_PROXIMAS = 50;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<EstadoFuncion, Set<EstadoFuncion>> TRANSICIONES = Map.of(
            EstadoFuncion.PROGRAMADA, Set.of(EstadoFuncion.EN_CURSO, EstadoFuncion.CANCELADA),
            EstadoFuncion.EN_CURSO, Set.of(EstadoFuncion.FINALIZADA),
            EstadoFuncion.FINALIZADA, Set.of(),
            EstadoFuncion.CANCELADA, Set.of());

    private final FuncionRepository funcionRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final DetalleBoletoService detalleBoletoService;

    public FuncionServiceImpl(FuncionRepository funcionRepository,
                              PeliculaRepository peliculaRepository,
                              SalaRepository salaRepository,
                              DetalleBoletoService detalleBoletoService) {
        this.funcionRepository = funcionRepository;
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
        this.detalleBoletoService = detalleBoletoService;
    }

    @Override
    public List<FuncionResponse> cartelera(LocalDate fecha) {
        return aResponses(funcionRepository.findCartelera(fecha, EstadoFuncion.PROGRAMADA));
    }

    @Override
    public List<FuncionResponse> proximasPorPelicula(Long idPelicula) {
        return aResponses(funcionRepository.findProximasPorPelicula(
                idPelicula, LocalDate.now(), EstadoFuncion.PROGRAMADA));
    }

    @Override
    public List<PeliculaResponse> peliculasEnCartelera() {
        return funcionRepository.findPeliculasEnCartelera(
                LocalDate.now(), EstadoFuncion.PROGRAMADA, EstadoPelicula.ACTIVA).stream()
                .map(PeliculaResponse::de)
                .toList();
    }

    @Override
    public Page<FuncionResponse> listar(Pageable pageable) {

        // Se limita el tamaño de página y el orden es fijo: lo más reciente primero
        int tamano = Math.min(Math.max(pageable.getPageSize(), 1), MAX_TAMANO_PAGINA);
        Pageable pagina = PageRequest.of(Math.max(pageable.getPageNumber(), 0), tamano,
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("horaInicio")));

        Page<Funcion> funciones = funcionRepository.findAll(pagina);

        return new PageImpl<>(aResponses(funciones.getContent()), pagina, funciones.getTotalElements());
    }

    @Override
    public List<FuncionResponse> proximas(int limite) {

        if (limite < 1 || limite > MAX_PROXIMAS) {
            throw new ReglaNegocioException("El límite debe estar entre 1 y " + MAX_PROXIMAS);
        }

        LocalDateTime ahora = LocalDateTime.now();   // una sola lectura del reloj

        return aResponses(funcionRepository.findProximas(
                EstadoFuncion.PROGRAMADA, ahora.toLocalDate(), ahora.toLocalTime(), PageRequest.of(0, limite)));
    }

    @Override
    public FuncionResponse buscarPorId(Long id) {
        Funcion funcion = obtenerFuncion(id);

        return FuncionResponse.de(funcion, detalleBoletoService.contarBoletos(id));
    }


    // ORDEN DE BLOQUEOS: función → sala, y siempre antes de cualquier otra consulta.
    // En MySQL (REPEATABLE READ) la primera lectura normal fija lo que la transacción verá;
    // un bloqueo tomado después no actualiza esa vista.
    @Override
    @Transactional
    public FuncionResponse crear(FuncionRequest request) {

        // 1. Bloqueo primero: ninguna otra lectura puede ir antes
        Sala sala = bloquearSala(request.idSala());

        // 2. Validaciones y lecturas, ya con el bloqueo tomado
        validarSalaActiva(sala);
        Pelicula pelicula = obtenerPeliculaActiva(request.idPelicula());

        validarNoEsPasado(request.fecha(), request.horaInicio());
        validarSinSolapamiento(sala.getId(), pelicula, request.fecha(), request.horaInicio(), null);

        Funcion funcion = new Funcion(pelicula, sala, request.fecha(), request.horaInicio());

        return FuncionResponse.de(funcionRepository.save(funcion), 0);   // una función nueva no tiene boletos
    }

    @Override
    @Transactional
    public FuncionResponse editar(Long id, FuncionRequest request) {

        // 1. Los dos bloqueos, en este orden y antes de cualquier otra lectura: función, luego sala
        Funcion funcion = bloquearFuncion(id);
        Sala sala = bloquearSala(request.idSala());

        // 2. Validaciones y lecturas, ya con ambos bloqueos tomados
        if (funcion.getEstado() != EstadoFuncion.PROGRAMADA) {
            throw new ReglaNegocioException("Solo se puede editar una función programada");
        }

        if (detalleBoletoService.tieneBoletosVendidos(id)) {
            throw new ReglaNegocioException(
                    "La función tiene boletos vendidos: cancélala y programa una nueva");
        }

        validarSalaActiva(sala);
        Pelicula pelicula = obtenerPeliculaActiva(request.idPelicula());

        validarNoEsPasado(request.fecha(), request.horaInicio());
        validarSinSolapamiento(sala.getId(), pelicula, request.fecha(), request.horaInicio(), id);

        funcion.setPelicula(pelicula);
        funcion.setSala(sala);
        funcion.setFecha(request.fecha());
        funcion.setHoraInicio(request.horaInicio());

        // sin save(): Hibernate guarda al terminar la transacción. 0 boletos: se comprobó arriba
        return FuncionResponse.de(funcion, 0);
    }

    @Override
    @Transactional
    public long cancelar(Long id) {

        Funcion funcion = funcionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoEncontradoException("La función no existe"));

        validarTransicion(funcion.getEstado(), EstadoFuncion.CANCELADA);

        funcion.setEstado(EstadoFuncion.CANCELADA);

        return detalleBoletoService.contarFacturasAfectadas(id);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoFuncion nuevoEstado) {

        if (nuevoEstado == EstadoFuncion.CANCELADA) {
            throw new ReglaNegocioException("Para cancelar una función usa la opción Cancelar");
        }

        Funcion funcion = funcionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoEncontradoException("La función no existe"));

        validarTransicion(funcion.getEstado(), nuevoEstado);

        funcion.setEstado(nuevoEstado);
    }

    // MÉTODOS PRIVADOS

    // Convierte las funciones a respuestas con los boletos vendidos de todas ellas en UNA sola consulta
    private List<FuncionResponse> aResponses(List<Funcion> funciones) {

        if (funciones.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> vendidos = detalleBoletoService.contarBoletosPorFuncion(
                funciones.stream().map(Funcion::getId).toList());

        return funciones.stream()
                .map(f -> FuncionResponse.de(f, vendidos.getOrDefault(f.getId(), 0L)))
                .toList();
    }

    private Funcion obtenerFuncion(Long id) {
        return funcionRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("La función no existe"));
    }

    private Pelicula obtenerPeliculaActiva(Long idPelicula) {
        Pelicula pelicula = peliculaRepository.findById(idPelicula)
                .orElseThrow(() -> new NoEncontradoException("La película no existe"));

        if (pelicula.getEstado() != EstadoPelicula.ACTIVA) {
            throw new ReglaNegocioException("La película no está activa");
        }
        return pelicula;
    }

    // Solo bloquean: las validaciones se hacen después, con el bloqueo ya tomado
    private Funcion bloquearFuncion(Long idFuncion) {
        return funcionRepository.findByIdForUpdate(idFuncion)
                .orElseThrow(() -> new NoEncontradoException("La función no existe"));
    }

    private Sala bloquearSala(Long idSala) {
        return salaRepository.findByIdForUpdate(idSala)
                .orElseThrow(() -> new NoEncontradoException("La sala no existe"));
    }

    private void validarSalaActiva(Sala sala) {
        if (sala.getEstado() != EstadoSala.ACTIVA) {
            throw new ReglaNegocioException("La sala no está activa");
        }
    }

    private void validarNoEsPasado(LocalDate fecha, LocalTime hora) {
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se puede programar una función en el pasado");
        }
    }

    private void validarTransicion(EstadoFuncion actual, EstadoFuncion nuevo) {
        if (!TRANSICIONES.get(actual).contains(nuevo)) {
            throw new ReglaNegocioException(
                    "No se puede pasar una función de " + actual + " a " + nuevo);
        }
    }

    // Cada función ocupa la sala desde su inicio hasta que termina la película + el margen de limpieza.
    // Se debe revisar el día anterior y el siguiente por si alguna función cruza la medianoche.
    private void validarSinSolapamiento(Long idSala, Pelicula pelicula, LocalDate fecha,
                                        LocalTime hora, Long idExcluir) {

        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        LocalDateTime fin = inicio.plusMinutes(pelicula.duracionEnMinutos() + MARGEN_MINUTOS);

        List<Funcion> vecinas = funcionRepository.findDeSalaEnRango(
                idSala, fecha.minusDays(1), fecha.plusDays(1), EstadoFuncion.CANCELADA);

        for (Funcion otra : vecinas) {
            if (otra.getId().equals(idExcluir)) {
                continue;   // al editar, la función no choca consigo misma
            }

            LocalDateTime otraInicio = LocalDateTime.of(otra.getFecha(), otra.getHoraInicio());
            LocalDateTime otraFin = otraInicio.plusMinutes(
                    otra.getPelicula().duracionEnMinutos() + MARGEN_MINUTOS);

            if (inicio.isBefore(otraFin) && otraInicio.isBefore(fin)) {
                throw new ReglaNegocioException(
                        "La sala está ocupada en ese horario: «" + otra.getPelicula().getTitulo()
                        + "» a las " + otra.getHoraInicio().format(HORA));
            }
        }
    }
}