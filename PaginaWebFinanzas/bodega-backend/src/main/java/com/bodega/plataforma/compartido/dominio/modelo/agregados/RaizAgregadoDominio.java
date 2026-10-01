package com.bodega.plataforma.compartido.dominio.modelo.agregados;

import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.Collection;

/**
 * Raíz de agregado de dominio con soporte para registrar eventos de dominio.
 */
public abstract class RaizAgregadoDominio<T extends RaizAgregadoDominio<T>> extends AbstractAggregateRoot<T> {

    protected void registrarEventoDominio(Object evento) {
        super.registerEvent(evento);
    }

    @Override
    public Collection<Object> domainEvents() {
        return super.domainEvents();
    }

    @Override
    public void clearDomainEvents() {
        super.clearDomainEvents();
    }
}
