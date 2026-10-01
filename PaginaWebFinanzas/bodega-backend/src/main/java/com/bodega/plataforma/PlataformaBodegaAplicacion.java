package com.bodega.plataforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación "Cuaderno de Fiado Digital".
 */
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class PlataformaBodegaAplicacion {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaBodegaAplicacion.class, args);
    }
}
