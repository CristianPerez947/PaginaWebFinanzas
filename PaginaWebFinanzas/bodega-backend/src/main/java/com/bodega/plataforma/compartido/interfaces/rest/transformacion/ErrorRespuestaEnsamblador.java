package com.bodega.plataforma.compartido.interfaces.rest.transformacion;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.interfaces.rest.recursos.ErrorRecurso;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Convierte un {@link ErrorAplicacion} en una respuesta HTTP con el estado adecuado.
 */
public final class ErrorRespuestaEnsamblador {

    private ErrorRespuestaEnsamblador() {
    }

    public static ResponseEntity<ErrorRecurso> aRespuestaDesdeError(ErrorAplicacion error) {
        var estado = estadoDe(error.codigo());
        return ResponseEntity.status(estado).body(new ErrorRecurso(error.codigo(), error.mensaje(), error.detalle()));
    }

    private static HttpStatus estadoDe(String codigo) {
        if (codigo.endsWith("_NO_ENCONTRADO")) return HttpStatus.NOT_FOUND;
        if (codigo.endsWith("_CONFLICTO")) return HttpStatus.CONFLICT;
        return switch (codigo) {
            case "ERROR_VALIDACION" -> HttpStatus.BAD_REQUEST;
            case "REGLA_NEGOCIO_VIOLADA" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "NO_AUTORIZADO" -> HttpStatus.UNAUTHORIZED;
            case "PROHIBIDO" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
