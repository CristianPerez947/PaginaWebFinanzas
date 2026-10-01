package com.bodega.plataforma.clientes.interfaces.rest.recursos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClienteConClaveRespuesta",
        description = "Cliente y su clave, visible una sola vez para que el vendedor se la comunique por teléfono")
public record ClienteConClaveRecurso(ClienteRecurso cliente, String clave) {
}
