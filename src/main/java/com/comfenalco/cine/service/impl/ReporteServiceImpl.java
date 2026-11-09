package com.comfenalco.cine.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.ReporteService;
import com.comfenalco.cine.dto.response.FuncionTaquilleraResponse;
import com.comfenalco.cine.dto.response.OcupacionSalaResponse;
import com.comfenalco.cine.dto.response.ResumenVentasResponse;
import com.comfenalco.cine.dto.resumen.TotalPorUsuario;
import com.comfenalco.cine.dto.response.VentaDiariaResponse;
import com.comfenalco.cine.dto.response.VentaPorPeliculaResponse;
import com.comfenalco.cine.dto.response.VentaPorProductoResponse;
import com.comfenalco.cine.dto.response.VentaPorTaquilleroResponse;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.enums.EstadoFactura;
import com.comfenalco.cine.model.enums.EstadoFuncion;
import com.comfenalco.cine.repository.DetalleBoletoRepository;
import com.comfenalco.cine.repository.DetalleConfiteriaRepository;
import com.comfenalco.cine.repository.FacturaRepository;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.SalaRepository;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private static final int MAX_DIAS_CONSULTA = 366;
    private static final int MAX_LIMITE = 50;

    private final FacturaRepository facturaRepository;
    private final DetalleBoletoRepository detalleBoletoRepository;
    private final DetalleConfiteriaRepository detalleConfiteriaRepository;
    private final FuncionRepository funcionRepository;
    private final SalaRepository salaRepository;

    public ReporteServiceImpl(FacturaRepository facturaRepository,
                              DetalleBoletoRepository detalleBoletoRepository,
                              DetalleConfiteriaRepository detalleConfiteriaRepository,
                              FuncionRepository funcionRepository,
                              SalaRepository salaRepository) {
        this.facturaRepository = facturaRepository;
        this.detalleBoletoRepository = detalleBoletoRepository;
        this.detalleConfiteriaRepository = detalleConfiteriaRepository;
        this.funcionRepository = funcionRepository;
        this.salaRepository = salaRepository;
    }

    @Override
    public ResumenVentasResponse resumen(LocalDate desde, LocalDate hasta) {

        validarRango(desde, hasta);
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();   // rango [inicio, fin)

        BigDecimal totalBoletos = detalleBoletoRepository.totalBoletosEnRango(inicio, fin);
        BigDecimal totalConfiteria = detalleConfiteriaRepository.totalConfiteriaEnRango(inicio, fin);

        return new ResumenVentasResponse(
                desde, hasta,
                totalBoletos, totalConfiteria, totalBoletos.add(totalConfiteria),
                facturaRepository.countByEstadoAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                        EstadoFactura.EMITIDA, inicio, fin),
                detalleBoletoRepository.contarBoletosEnRango(inicio, fin),
                funcionRepository.countByEstadoAndFechaBetween(EstadoFuncion.FINALIZADA, desde, hasta));
    }

    @Override
    public List<VentaPorPeliculaResponse> boletosPorPelicula(LocalDate desde, LocalDate hasta, int limite) {

        validarRango(desde, hasta);
        validarLimite(limite);

        return detalleBoletoRepository.boletosPorPelicula(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), PageRequest.of(0, limite));
    }

    @Override
    public List<VentaPorProductoResponse> productosMasVendidos(LocalDate desde, LocalDate hasta, int limite) {

        validarRango(desde, hasta);
        validarLimite(limite);

        return detalleConfiteriaRepository.productosMasVendidos(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), PageRequest.of(0, limite));
    }

    @Override
    public List<VentaDiariaResponse> ventasPorDia(LocalDate desde, LocalDate hasta) {

        validarRango(desde, hasta);
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        Map<LocalDate, BigDecimal> boletos = aMapa(detalleBoletoRepository.totalBoletosPorDia(inicio, fin));
        Map<LocalDate, BigDecimal> confiteria = aMapa(detalleConfiteriaRepository.totalConfiteriaPorDia(inicio, fin));

        List<VentaDiariaResponse> serie = new ArrayList<>();

        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            BigDecimal b = boletos.getOrDefault(dia, BigDecimal.ZERO);
            BigDecimal c = confiteria.getOrDefault(dia, BigDecimal.ZERO);
            serie.add(new VentaDiariaResponse(dia, b, c, b.add(c)));
        }

        return serie;
    }

    @Override
    public List<OcupacionSalaResponse> ocupacionPorSala(LocalDate desde, LocalDate hasta) {

        validarRango(desde, hasta);

        return salaRepository.ocupacionPorSala(desde, hasta, EstadoFuncion.CANCELADA);
    }

    @Override
    public List<VentaPorTaquilleroResponse> ventasPorTaquillero(LocalDate desde, LocalDate hasta) {

        validarRango(desde, hasta);
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        Map<Long, BigDecimal> boletos = aMapaPorUsuario(detalleBoletoRepository.totalBoletosPorUsuario(inicio, fin));
        Map<Long, BigDecimal> confiteria = aMapaPorUsuario(detalleConfiteriaRepository.totalConfiteriaPorUsuario(inicio, fin));

        return facturaRepository.facturasPorUsuario(inicio, fin).stream()
                .map(conteo -> {
                    BigDecimal b = boletos.getOrDefault(conteo.idUsuario(), BigDecimal.ZERO);
                    BigDecimal c = confiteria.getOrDefault(conteo.idUsuario(), BigDecimal.ZERO);
                    return new VentaPorTaquilleroResponse(conteo.nombre(), conteo.facturas(), b, c, b.add(c));
                })
                .sorted(Comparator.comparing(VentaPorTaquilleroResponse::total, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public List<FuncionTaquilleraResponse> funcionesMasTaquilleras(LocalDate desde, LocalDate hasta, int limite) {

        validarRango(desde, hasta);
        validarLimite(limite);

        return detalleBoletoRepository.funcionesMasTaquilleras(desde, hasta, PageRequest.of(0, limite));
    }

    // MÉTODOS PRIVADOS

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new ReglaNegocioException("El rango de fechas no es válido");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_DIAS_CONSULTA) {
            throw new ReglaNegocioException("El rango máximo es de " + MAX_DIAS_CONSULTA + " días");
        }
    }

    private Map<Long, BigDecimal> aMapaPorUsuario(List<TotalPorUsuario> filas) {
        Map<Long, BigDecimal> mapa = new HashMap<>();

        for (TotalPorUsuario fila : filas) {
            mapa.put(fila.idUsuario(), fila.total());
        }

        return mapa;
    }

    private void validarLimite(int limite) {
        if (limite < 1 || limite > MAX_LIMITE) {
            throw new ReglaNegocioException("El límite debe estar entre 1 y " + MAX_LIMITE);
        }
    }

    // Cada fila es [año, mes, día, total]
    private Map<LocalDate, BigDecimal> aMapa(List<Object[]> filas) {
        Map<LocalDate, BigDecimal> mapa = new HashMap<>();

        for (Object[] fila : filas) {
            LocalDate dia = LocalDate.of(
                    ((Number) fila[0]).intValue(),
                    ((Number) fila[1]).intValue(),
                    ((Number) fila[2]).intValue());
            mapa.put(dia, (BigDecimal) fila[3]);
        }
        return mapa;
    }
}