package com.comfenalco.cine.dto.response;

import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.EstadoUsuario;
import com.comfenalco.cine.model.enums.RolUsuario;

public record UsuarioResponse(
    Long id, String nombreCompleto, RolUsuario rol, EstadoUsuario estado
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombreCompleto(),
                                   u.getRol(), u.getEstado());
    }
}
