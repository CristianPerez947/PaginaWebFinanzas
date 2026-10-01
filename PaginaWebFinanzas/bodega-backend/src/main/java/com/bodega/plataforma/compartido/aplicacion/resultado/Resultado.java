package com.bodega.plataforma.compartido.aplicacion.resultado;

import java.util.Optional;
import java.util.function.Function;

/**
 * Resultado de una operación de aplicación: éxito con un valor o fallo con un error.
 */
public sealed interface Resultado<T, E> {

    record Exito<T, E>(T valor) implements Resultado<T, E> {
    }

    record Fallo<T, E>(E error) implements Resultado<T, E> {
    }

    static <T, E> Resultado<T, E> exito(T valor) {
        return new Exito<>(valor);
    }

    static <T, E> Resultado<T, E> fallo(E error) {
        return new Fallo<>(error);
    }

    default boolean esExito() {
        return this instanceof Exito;
    }

    default boolean esFallo() {
        return this instanceof Fallo;
    }

    default Optional<T> aOpcional() {
        return switch (this) {
            case Exito<T, E> e -> Optional.ofNullable(e.valor);
            case Fallo<T, E> f -> Optional.empty();
        };
    }

    default <T2> Resultado<T2, E> mapear(Function<T, T2> f) {
        return switch (this) {
            case Exito<T, E> e -> Resultado.exito(f.apply(e.valor));
            case Fallo<T, E> fallo -> Resultado.fallo(fallo.error);
        };
    }

    default <T2> Resultado<T2, E> encadenar(Function<T, Resultado<T2, E>> f) {
        return switch (this) {
            case Exito<T, E> e -> f.apply(e.valor);
            case Fallo<T, E> fallo -> Resultado.fallo(fallo.error);
        };
    }
}
