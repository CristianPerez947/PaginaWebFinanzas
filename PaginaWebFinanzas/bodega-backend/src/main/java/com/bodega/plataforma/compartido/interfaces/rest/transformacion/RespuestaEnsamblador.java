package com.bodega.plataforma.compartido.interfaces.rest.transformacion;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Convierte un {@link Resultado} en una respuesta HTTP.
 */
public final class RespuestaEnsamblador {

    private RespuestaEnsamblador() {
    }

    public static <T, R> ResponseEntity<?> aRespuestaDesdeResultado(
            Resultado<T, ErrorAplicacion> resultado,
            Function<T, R> ensambladorRecurso,
            HttpStatusCode estadoExito) {
        return switch (resultado) {
            case Resultado.Exito<T, ErrorAplicacion> exito ->
                    new ResponseEntity<>(ensambladorRecurso.apply(exito.valor()), estadoExito);
            case Resultado.Fallo<T, ErrorAplicacion> fallo ->
                    ErrorRespuestaEnsamblador.aRespuestaDesdeError(fallo.error());
        };
    }
}
