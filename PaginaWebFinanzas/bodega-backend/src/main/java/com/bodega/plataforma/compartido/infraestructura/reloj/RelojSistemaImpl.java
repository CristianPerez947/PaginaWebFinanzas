package com.bodega.plataforma.compartido.infraestructura.reloj;

import com.bodega.plataforma.compartido.dominio.servicios.RelojSistema;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Reloj del sistema con zona horaria de Lima. Si el reloj simulado está activo, se devuelve la
 * fecha simulada configurada por el administrador (modo demostración).
 */
@Service
public class RelojSistemaImpl implements RelojSistema {

    public static final ZoneId ZONA = ZoneId.of("America/Lima");

    private final ConfiguracionSistemaRepositorio repositorio;

    public RelojSistemaImpl(ConfiguracionSistemaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public LocalDateTime ahora() {
        var configuracion = repositorio.findAll().stream().findFirst();
        if (configuracion.isPresent() && configuracion.get().isRelojSimuladoActivo()
                && configuracion.get().getFechaSimulada() != null) {
            return configuracion.get().getFechaSimulada();
        }
        return LocalDateTime.now(ZONA);
    }

    public void simular(LocalDateTime fecha) {
        var configuracion = obtenerConfiguracion();
        configuracion.setRelojSimuladoActivo(true);
        configuracion.setFechaSimulada(fecha);
        repositorio.save(configuracion);
    }

    public void restaurarRelojReal() {
        var configuracion = obtenerConfiguracion();
        configuracion.setRelojSimuladoActivo(false);
        configuracion.setFechaSimulada(null);
        repositorio.save(configuracion);
    }

    public boolean estaSimulado() {
        return repositorio.findAll().stream().findFirst()
                .map(ConfiguracionSistemaEntidad::isRelojSimuladoActivo).orElse(false);
    }

    private ConfiguracionSistemaEntidad obtenerConfiguracion() {
        return repositorio.findAll().stream().findFirst().orElseGet(ConfiguracionSistemaEntidad::new);
    }
}
