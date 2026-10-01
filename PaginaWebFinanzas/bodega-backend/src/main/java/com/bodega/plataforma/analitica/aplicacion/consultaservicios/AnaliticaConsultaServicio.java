package com.bodega.plataforma.analitica.aplicacion.consultaservicios;

import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaDeudor;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaInteresMensual;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaMoroso;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.FilaProductoVendido;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.PanelVendedor;
import com.bodega.plataforma.analitica.dominio.modelo.vistas.VistasAnalitica.ResumenCartera;

import java.util.List;

/**
 * Consultas de solo lectura para el panel y los reportes del vendedor.
 */
public interface AnaliticaConsultaServicio {
    PanelVendedor panel(Long tiendaId);

    List<FilaDeudor> deudores(Long tiendaId);

    List<FilaMoroso> morosos(Long tiendaId);

    List<FilaProductoVendido> ventasPorProducto(Long tiendaId);

    List<FilaInteresMensual> intereses(Long tiendaId);

    ResumenCartera cartera(Long tiendaId);
}
