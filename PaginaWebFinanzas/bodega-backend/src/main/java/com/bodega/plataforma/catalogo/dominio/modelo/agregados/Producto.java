package com.bodega.plataforma.catalogo.dominio.modelo.agregados;

import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CrearProductoComando;
import com.bodega.plataforma.compartido.dominio.modelo.agregados.RaizAgregadoDominio;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Raíz de agregado Producto: producto o servicio que la tienda vende de contado o a crédito.
 * La baja es lógica (activo = false).
 */
@Getter
public class Producto extends RaizAgregadoDominio<Producto> {

    @Setter
    private Long id;
    @Setter
    private Long tiendaId;
    @Setter
    private String nombre;
    @Setter
    private String categoria;
    @Setter
    private String marca;
    @Setter
    private String proveedor;
    @Setter
    private String descripcion;
    @Setter
    private String unidadMedida;
    @Setter
    private String imagen;
    @Setter
    private BigDecimal precioContado;
    @Setter
    private BigDecimal precioCredito;
    @Setter
    private boolean permiteCuotas;
    @Setter
    private boolean activo = true;

    public Producto() {
    }

    public Producto(CrearProductoComando c) {
        this.tiendaId = c.tiendaId();
        actualizarInformacion(c.nombre(), c.categoria(), c.marca(), c.proveedor(), c.descripcion(), c.unidadMedida(),
                c.imagen(), c.precioContado(), c.precioCredito(), c.permiteCuotas());
        this.activo = true;
    }

    public Producto actualizarInformacion(String nombre, String categoria, String marca, String proveedor,
                                          String descripcion, String unidadMedida, String imagen,
                                          BigDecimal precioContado, BigDecimal precioCredito, boolean permiteCuotas) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.proveedor = proveedor;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.imagen = imagen;
        this.precioContado = precioContado;
        this.precioCredito = precioCredito;
        this.permiteCuotas = permiteCuotas;
        return this;
    }

    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }
}
