package com.bodega.plataforma.identidad.aplicacion.interno.manejadoreseventos;

import com.bodega.plataforma.identidad.aplicacion.comandoservicios.UsuarioComandoServicio;
import com.bodega.plataforma.identidad.dominio.modelo.comandos.CrearUsuarioComando;
import com.bodega.plataforma.identidad.dominio.modelo.objetosvalor.Rol;
import com.bodega.plataforma.identidad.dominio.repositorios.UsuarioRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial del sistema al arrancar, si todavía no existe ninguno.
 */
@Slf4j
@Component
public class InicioAplicacionManejador {

    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioComandoServicio usuarioComandoServicio;
    private final String usuarioAdmin;
    private final String claveAdmin;

    public InicioAplicacionManejador(UsuarioRepositorio usuarioRepositorio,
                                     UsuarioComandoServicio usuarioComandoServicio,
                                     @Value("${semilla.admin.usuario}") String usuarioAdmin,
                                     @Value("${semilla.admin.clave}") String claveAdmin) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.usuarioComandoServicio = usuarioComandoServicio;
        this.usuarioAdmin = usuarioAdmin;
        this.claveAdmin = claveAdmin;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void alIniciar() {
        if (usuarioRepositorio.existePorRol(Rol.ADMIN_SISTEMA)) return;
        var resultado = usuarioComandoServicio.manejar(
                new CrearUsuarioComando(usuarioAdmin, claveAdmin, Rol.ADMIN_SISTEMA, null));
        log.info("Administrador inicial del sistema creado: {}", resultado.esExito());
    }
}
