package com.bodega.plataforma.identidad.interfaces.rest;

import com.bodega.plataforma.identidad.aplicacion.comandoservicios.UsuarioComandoServicio;
import com.bodega.plataforma.identidad.dominio.modelo.agregados.Usuario;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.IniciarSesionComando;
import com.bodega.plataforma.identidad.interfaces.rest.recursos.IniciarSesionClienteRecurso;
import com.bodega.plataforma.identidad.interfaces.rest.recursos.IniciarSesionRecurso;
import com.bodega.plataforma.identidad.interfaces.rest.transformacion.SesionRecursoEnsamblador;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Autenticación: acceso del personal (administrador y vendedor) y acceso de clientes por tienda.
 */
@RestController
@RequestMapping(value = "/api/v1/autenticacion", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Autenticación", description = "Inicio de sesión con JWT")
public class AutenticacionController {

    private final UsuarioComandoServicio usuarioComandoServicio;

    public AutenticacionController(UsuarioComandoServicio usuarioComandoServicio) {
        this.usuarioComandoServicio = usuarioComandoServicio;
    }

    @PostMapping("/iniciar-sesion")
    @Operation(summary = "Inicio de sesión del administrador del sistema o del vendedor")
    public ResponseEntity<?> iniciarSesion(@RequestBody IniciarSesionRecurso recurso) {
        var resultado = usuarioComandoServicio.manejar(
                new IniciarSesionComando(recurso.usuario(), recurso.clave(), false));
        return RespuestaEnsamblador.aRespuestaDesdeResultado(resultado,
                SesionRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }

    @PostMapping("/cliente")
    @Operation(summary = "Inicio de sesión de un cliente (tienda + DNI + clave)")
    public ResponseEntity<?> iniciarSesionCliente(@RequestBody IniciarSesionClienteRecurso recurso) {
        if (recurso.tiendaId() == null || recurso.dni() == null)
            throw new IllegalArgumentException("La tienda y el DNI son obligatorios");
        var nombreUsuario = Usuario.nombreParaCliente(recurso.tiendaId(), recurso.dni().trim());
        var resultado = usuarioComandoServicio.manejar(new IniciarSesionComando(nombreUsuario, recurso.clave(), true));
        return RespuestaEnsamblador.aRespuestaDesdeResultado(resultado,
                SesionRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }
}
