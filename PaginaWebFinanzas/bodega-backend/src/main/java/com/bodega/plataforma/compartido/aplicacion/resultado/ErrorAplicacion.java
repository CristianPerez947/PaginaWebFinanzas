package com.bodega.plataforma.compartido.aplicacion.resultado;

/**
 * Error tipado de la capa de aplicación. El código determina el estado HTTP.
 */
public record ErrorAplicacion(String codigo, String mensaje, String detalle) {

    public static ErrorAplicacion validacion(String campo, String motivo) {
        return new ErrorAplicacion("ERROR_VALIDACION", "Datos inválidos: %s".formatted(campo), motivo);
    }

    public static ErrorAplicacion noEncontrado(String recurso, String identificador) {
        return new ErrorAplicacion("%s_NO_ENCONTRADO".formatted(normalizar(recurso)),
                "%s no encontrado: %s".formatted(recurso, identificador), null);
    }

    public static ErrorAplicacion reglaDeNegocio(String regla, String motivo) {
        return new ErrorAplicacion("REGLA_NEGOCIO_VIOLADA", "No permitido: %s".formatted(regla), motivo);
    }

    public static ErrorAplicacion conflicto(String recurso, String motivo) {
        return new ErrorAplicacion("%s_CONFLICTO".formatted(normalizar(recurso)),
                "Conflicto con %s".formatted(recurso), motivo);
    }

    public static ErrorAplicacion noAutorizado(String motivo) {
        return new ErrorAplicacion("NO_AUTORIZADO", "Credenciales inválidas", motivo);
    }

    public static ErrorAplicacion prohibido(String motivo) {
        return new ErrorAplicacion("PROHIBIDO", "No tiene permiso para esta operación", motivo);
    }

    public static ErrorAplicacion inesperado(String contexto, String motivo) {
        return new ErrorAplicacion("ERROR_INESPERADO", "Error inesperado en %s".formatted(contexto), motivo);
    }

    private static String normalizar(String recurso) {
        return recurso.toUpperCase().replace(' ', '_');
    }
}
