package com.bodega.plataforma.compartido.interfaces.rest;

import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.ErrorRespuestaEnsamblador;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo centralizado de excepciones para toda la API.
 */
@Slf4j
@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> manejarValidacion(MethodArgumentNotValidException ex) {
        var detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .reduce((a, b) -> a + "; " + b)
                .orElse("La solicitud no es válida");
        return ErrorRespuestaEnsamblador.aRespuestaDesdeError(ErrorAplicacion.validacion("cuerpo de la solicitud", detalle));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ErrorRespuestaEnsamblador.aRespuestaDesdeError(ErrorAplicacion.validacion("argumento", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> manejarAccesoDenegado(AccessDeniedException ex) {
        return ErrorRespuestaEnsamblador.aRespuestaDesdeError(ErrorAplicacion.prohibido("Su rol no permite esta operación"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> manejarInesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return ErrorRespuestaEnsamblador.aRespuestaDesdeError(
                ErrorAplicacion.inesperado("servidor", ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado"));
    }
}
