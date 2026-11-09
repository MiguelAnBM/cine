package com.comfenalco.cine.dto.response;

import java.math.BigDecimal;

import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.enums.EstadoSala;
import com.comfenalco.cine.model.enums.TipoSala;

public record SalaResponse(
    Long id, String nombre, TipoSala tipo, BigDecimal precioBoleto, EstadoSala estado
) {
    public static SalaResponse de(Sala s) {
        return new SalaResponse(
                s.getId(), s.getNombre(), s.getTipo(), s.getPrecioBoleto(), s.getEstado());
    }
}
