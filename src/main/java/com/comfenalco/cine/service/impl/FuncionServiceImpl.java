package com.comfenalco.cine.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.DetalleBoletoService;
import com.comfenalco.cine.service.FuncionService;
import com.comfenalco.cine.service.FuncionVendibilidad;
import com.comfenalco.cine.dto.request.FuncionRequest;
import com.comfenalco.cine.dto.response.FuncionResponse;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.Funcion;
import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.enums.EstadoPelicula;
import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.PeliculaRepository;
import com.comfenalco.cine.repository.SalaRepository;

import java.time.Clock;

@Service
@Transactional(readOnly = true)
public class FuncionServiceImpl implements FuncionService {

    private static final int MARGEN_MINUTOS = 15;

    private static final int MAX_TAMANO_PAGINA = 100;
    private static final int MAX_PROXIMAS = 50;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final FuncionRepository funcionRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final DetalleBoletoService detalleBoletoService;
    private final Clock reloj;

    public FuncionServiceImpl(FuncionRepository funcionRepository,
                              PeliculaRepository peliculaRepository,
                              SalaRepository salaRepository,
                              DetalleBoletoService detalleBoletoService,
                              Clock reloj) {
        this.funcionRepository = funcionRepository;
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
        this.detalleBoletoService = detalleBoletoService;
        this.reloj = reloj;
    }

    @Override
    public Page<FuncionResponse> listar(Pageable pageable) {
        int tamano = Math.min(Math.max(pageable.getPageSize(), 1), MAX_TAMANO_PAGINA);
        Pageable pagina = PageRequest.of(Math.max(pageable.getPageNumber(), 0), tamano,
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("horaInicio")));

        LocalDateTime ahora = LocalDateTime.now(reloj);
        Page<Funcion> funciones = funcionRepository.findAll(pagina);

        return new PageImpl<>(aResponses(funciones.getContent(), ahora), pagina, funciones.getTotalElements());
    }

    @Override
    public List<FuncionResponse> proximas(int limite) {
        if (limite < 1 || limite > MAX_PROXIMAS) {
            throw new ReglaNegocioException("El límite debe estar entre 1 y " + MAX_PROXIMAS);
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime desde = ahora.minusMinutes(FuncionVendibilidad.MINUTOS_VENTA_TRAS_INICIO);

        List<Funcion> todas = funcionRepository.findProximas(
                desde.toLocalDate(), desde.toLocalTime(), PageRequest.of(0, limite));

        return aResponses(todas.stream()
                .filter(f -> FuncionVendibilidad.esVendible(
                        LocalDateTime.of(f.getFecha(), f.getHoraInicio()),
                        f.getPelicula().duracionEnMinutos(),
                        ahora))
                .toList(), ahora);
    }

    @Override
    public List<FuncionResponse> proximasDeHoy(int limite) {
        if (limite < 1 || limite > MAX_PROXIMAS) {
            throw new ReglaNegocioException("El límite debe estar entre 1 y " + MAX_PROXIMAS);
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime desde = ahora.minusMinutes(FuncionVendibilidad.MINUTOS_VENTA_TRAS_INICIO);

        List<Funcion> todas = funcionRepository.findProximasDeHoy(
                desde.toLocalDate(), desde.toLocalTime(), PageRequest.of(0, limite));

        return aResponses(todas.stream()
                .filter(f -> FuncionVendibilidad.esVendible(
                        LocalDateTime.of(f.getFecha(), f.getHoraInicio()),
                        f.getPelicula().duracionEnMinutos(),
                        ahora))
                .toList(), ahora);
    }

    @Override
    public FuncionResponse buscarPorId(Long id) {
        Funcion funcion = obtenerFuncion(id);
        return FuncionResponse.de(funcion, detalleBoletoService.contarBoletos(id), LocalDateTime.now(reloj));
    }

    @Override
    @Transactional
    public FuncionResponse crear(FuncionRequest request) {
        Sala sala = bloquearSala(request.idSala());

        validarSalaActiva(sala);
        Pelicula pelicula = obtenerPeliculaActiva(request.idPelicula());

        validarNoEsPasado(request.fecha(), request.horaInicio());
        validarSinSolapamiento(sala.getId(), pelicula, request.fecha(), request.horaInicio(), null);

        Funcion funcion = new Funcion(pelicula, sala, request.fecha(), request.horaInicio());

        return FuncionResponse.de(funcionRepository.save(funcion), 0, LocalDateTime.now(reloj));
    }

    @Override
    @Transactional
    public FuncionResponse editar(Long id, FuncionRequest request) {
        Funcion funcion = bloquearFuncion(id);
        Sala sala = bloquearSala(request.idSala());

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime inicio = LocalDateTime.of(funcion.getFecha(), funcion.getHoraInicio());
        if (FuncionVendibilidad.haFinalizado(inicio, funcion.getPelicula().duracionEnMinutos(), ahora)) {
            throw new ReglaNegocioException("No se puede editar una función que ya finalizó");
        }

        if (detalleBoletoService.tieneBoletosVendidos(id)) {
            throw new ReglaNegocioException(
                    "La función tiene boletos vendidos y no se puede editar");
        }

        validarSalaActiva(sala);
        Pelicula pelicula = obtenerPeliculaActiva(request.idPelicula());

        validarNoEsPasado(request.fecha(), request.horaInicio());
        validarSinSolapamiento(sala.getId(), pelicula, request.fecha(), request.horaInicio(), id);

        funcion.setPelicula(pelicula);
        funcion.setSala(sala);
        funcion.setFecha(request.fecha());
        funcion.setHoraInicio(request.horaInicio());

        return FuncionResponse.de(funcion, 0, LocalDateTime.now(reloj));
    }

    @Override
    public void validarVendible(Long idFuncion) {
        Funcion funcion = obtenerFuncion(idFuncion);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime inicio = LocalDateTime.of(funcion.getFecha(), funcion.getHoraInicio());

        if (!FuncionVendibilidad.esVendible(inicio, funcion.getPelicula().duracionEnMinutos(), ahora)) {
            throw new ReglaNegocioException("No se pueden vender boletos para esta función");
        }
    }

    @Override
    public long contarFuncionesDelDia(LocalDate fecha) {
        if (fecha == null) {
            return 0;
        }
        return funcionRepository.countByFecha(fecha);
    }

    private List<FuncionResponse> aResponses(List<Funcion> funciones, LocalDateTime ahora) {
        if (funciones.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> vendidos = detalleBoletoService.contarBoletosPorFuncion(
                funciones.stream().map(Funcion::getId).toList());

        return funciones.stream()
                .map(f -> FuncionResponse.de(f, vendidos.getOrDefault(f.getId(), 0L), ahora))
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
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("No se puede programar una función en el pasado");
        }
    }

    private void validarSinSolapamiento(Long idSala, Pelicula pelicula, LocalDate fecha,
                                        LocalTime hora, Long idExcluir) {

        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        LocalDateTime fin = inicio.plusMinutes(pelicula.duracionEnMinutos() + MARGEN_MINUTOS);

        List<Funcion> vecinas = funcionRepository.findDeSalaEnRango(
                idSala, fecha.minusDays(1), fecha.plusDays(1));

        for (Funcion otra : vecinas) {
            if (otra.getId().equals(idExcluir)) {
                continue;
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
