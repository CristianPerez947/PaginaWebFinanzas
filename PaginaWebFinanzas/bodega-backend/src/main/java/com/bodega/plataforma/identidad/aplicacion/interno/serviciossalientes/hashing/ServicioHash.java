package com.bodega.plataforma.identidad.aplicacion.interno.serviciossalientes.hashing;

/**
 * Servicio saliente para cifrar y verificar claves.
 */
public interface ServicioHash {
    String cifrar(String claveTextoPlano);

    boolean coincide(String claveTextoPlano, String claveCifrada);
}
