package com.bodega.plataforma.identidad.infraestructura.hashing.bcrypt;

import com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.hashing.ServicioHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cifrado de claves con BCrypt.
 */
@Component
public class ServicioHashBCrypt implements ServicioHash {

    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String claveTextoPlano) {
        return codificador.encode(claveTextoPlano);
    }

    @Override
    public boolean coincide(String claveTextoPlano, String claveCifrada) {
        return codificador.matches(claveTextoPlano, claveCifrada);
    }
}
