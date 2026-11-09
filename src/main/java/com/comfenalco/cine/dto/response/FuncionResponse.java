package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.comfenalco.cine.model.Funcion;
import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.enums.EstadoFuncion;
import com.comfenalco.cine.model.enums.TipoSala;

public record FuncionResponse(
    Long id,
    Long idPelicula, String pelicula, String duracion,
    Long idSala, String sala, TipoSala tipoSala, BigDecimal precioBoleto,
    LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
    EstadoFuncion estado,
    long boletosVendidos
) {
    // Debe llamarse dentro de la transacción del servicio (recorre relaciones LAZY)
    public static FuncionResponse de(Funcion f, long boletosVendidos) {
        Pelicula p = f.getPelicula();
        Sala s = f.getSala();

        return new FuncionResponse(
                f.getId(),
                p.getId(), p.getTitulo(), p.getDuracion(),
                s.getId(), s.getNombre(), s.getTipo(), s.getPrecioBoleto(),
                f.getFecha(), f.getHoraInicio(),
                f.getHoraInicio().plusMinutes(p.duracionEnMinutos()),   // si cruza la medianoche, vuelve a 00:xx
                f.getEstado(),
                boletosVendidos);
    }
}