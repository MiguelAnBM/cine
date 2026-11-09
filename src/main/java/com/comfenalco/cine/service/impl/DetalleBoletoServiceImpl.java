package com.comfenalco.cine.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.service.DetalleBoletoService;
import com.comfenalco.cine.dto.response.AsientoResponse;
import com.comfenalco.cine.dto.resumen.ConteoPorFuncion;
import com.comfenalco.cine.exception.NoEncontradoException;
import com.comfenalco.cine.exception.ReglaNegocioException;
import com.comfenalco.cine.model.Asientos;
import com.comfenalco.cine.repository.DetalleBoletoRepository;
import com.comfenalco.cine.repository.FuncionRepository;

@Service
@Transactional(readOnly = true)
public class DetalleBoletoServiceImpl implements DetalleBoletoService {

    // Se construye una sola vez: A1..H10
    private static final Set<String> ASIENTOS_VALIDOS = Set.copyOf(Asientos.todos());

    private final DetalleBoletoRepository detalleBoletoRepository;
    private final FuncionRepository funcionRepository;

    public DetalleBoletoServiceImpl(DetalleBoletoRepository detalleBoletoRepository,
                                    FuncionRepository funcionRepository) {
        this.detalleBoletoRepository = detalleBoletoRepository;
        this.funcionRepository = funcionRepository;
    }

    @Override
    public List<AsientoResponse> mapaDeAsientos(Long idFuncion) {

        if (!funcionRepository.existsById(idFuncion)) {
            throw new NoEncontradoException("La función no existe");
        }

        // Set para que contains() no recorra la lista 80 veces
        Set<String> ocupados = new HashSet<>(detalleBoletoRepository.findAsientosOcupados(idFuncion));

        return Asientos.todos().stream()
                .map(codigo -> new AsientoResponse(codigo, ocupados.contains(codigo)))
                .toList();
    }

    @Override
    public boolean estaDisponible(Long idFuncion, String codigoAsiento) {

        String codigo = normalizar(codigoAsiento);

        if (codigo == null || !ASIENTOS_VALIDOS.contains(codigo)) {
            return false;
        }

        return !detalleBoletoRepository.existsByFuncionIdAndAsientoActivo(idFuncion, codigo);
    }

    @Override
    public void validarDisponibles(Long idFuncion, Collection<String> codigosAsiento) {

        if (codigosAsiento == null || codigosAsiento.isEmpty()) {
            throw new ReglaNegocioException("Selecciona al menos un asiento");
        }

        // Una sola consulta para toda la selección
        Set<String> ocupados = new HashSet<>(detalleBoletoRepository.findAsientosOcupados(idFuncion));
        Set<String> vistos = new HashSet<>();

        for (String original : codigosAsiento) {
            String codigo = normalizar(original);

            if (codigo == null || !ASIENTOS_VALIDOS.contains(codigo)) {
                throw new ReglaNegocioException("La selección incluye un asiento que no existe");
            }

            if (!vistos.add(codigo)) {
                throw new ReglaNegocioException("El asiento " + codigo + " está repetido en la selección");
            }

            if (ocupados.contains(codigo)) {
                throw new ReglaNegocioException("El asiento " + codigo + " ya fue vendido");
            }
        }
    }

    @Override
    public boolean tieneBoletosVendidos(Long idFuncion) {
        return detalleBoletoRepository.existsByFuncionIdAndAsientoActivoIsNotNull(idFuncion);
    }

    @Override
    public long contarFacturasAfectadas(Long idFuncion) {
        return detalleBoletoRepository.contarFacturasAfectadas(idFuncion);
    }

    @Override
    public long contarBoletos(Long idFuncion) {
        return detalleBoletoRepository.countByFuncionIdAndAsientoActivoIsNotNull(idFuncion);
    }

    @Override
    public Map<Long, Long> contarBoletosPorFuncion(Collection<Long> idsFuncion) {

        if (idsFuncion == null || idsFuncion.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> conteos = new HashMap<>();

        for (ConteoPorFuncion fila : detalleBoletoRepository.contarBoletosPorFuncion(idsFuncion)) {
            conteos.put(fila.idFuncion(), fila.boletos());
        }

        return conteos;
    }

    // MÉTODOS PRIVADOS

    // Convierte a1 -> A1
    private String normalizar(String codigo) {
        return (codigo == null) ? null : codigo.trim().toUpperCase(Locale.ROOT);
    }
}