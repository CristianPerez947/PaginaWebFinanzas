package com.bodega.plataforma.catalogo.interfaces.rest;

import com.bodega.plataforma.catalogo.aplicacion.comandoservicios.ProductoComandoServicio;
import com.bodega.plataforma.catalogo.aplicacion.consultaservicios.ProductoConsultaServicio;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CambiarEstadoProductoComando;
import com.bodega.plataforma.catalogo.dominio.modelo.consultas.ObtenerProductoPorIdConsulta;
import com.bodega.plataforma.catalogo.dominio.modelo.consultas.ObtenerProductosPorTiendaConsulta;
import com.bodega.plataforma.catalogo.interfaces.rest.recursos.CambiarEstadoProductoRecurso;
import com.bodega.plataforma.catalogo.interfaces.rest.recursos.GuardarProductoRecurso;
import com.bodega.plataforma.catalogo.interfaces.rest.recursos.ProductoRecurso;
import com.bodega.plataforma.catalogo.interfaces.rest.transformacion.ProductoRecursoEnsamblador;
import com.bodega.plataforma.compartido.interfaces.rest.transformacion.RespuestaEnsamblador;
import com.bodega.plataforma.compartido.interfaces.seguridad.UsuarioActual;
import com.bodega.plataforma.tiendas.interfaces.acl.TiendasContextoFachada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Catálogo de productos/servicios. La gestión es exclusiva del vendedor de la tienda; el listado público
 * de productos activos está disponible sin autenticación por el identificador de URL de la tienda.
 */
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
@Tag(name = "Productos", description = "Catálogo de productos y servicios de la tienda")
public class ProductosController {

    private final ProductoComandoServicio comandoServicio;
    private final ProductoConsultaServicio consultaServicio;
    private final TiendasContextoFachada tiendasFachada;

    public ProductosController(ProductoComandoServicio comandoServicio, ProductoConsultaServicio consultaServicio,
                               TiendasContextoFachada tiendasFachada) {
        this.comandoServicio = comandoServicio;
        this.consultaServicio = consultaServicio;
        this.tiendasFachada = tiendasFachada;
    }

    @GetMapping("/api/v1/publico/tiendas/{slug}/productos")
    @Operation(summary = "Catálogo público: productos activos de una tienda")
    public ResponseEntity<List<ProductoRecurso>> catalogoPublico(@PathVariable String slug) {
        var tiendaId = tiendasFachada.obtenerTiendaActivaIdPorSlug(slug);
        if (tiendaId.isEmpty()) return ResponseEntity.notFound().build();
        var productos = consultaServicio.manejar(new ObtenerProductosPorTiendaConsulta(tiendaId.get(), true)).stream()
                .map(ProductoRecursoEnsamblador::aRecursoDesdeEntidad).toList();
        return ResponseEntity.ok(productos);
    }

    @PostMapping("/api/v1/productos")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Da de alta un producto")
    public ResponseEntity<?> crear(@RequestBody GuardarProductoRecurso recurso) {
        var comando = ProductoRecursoEnsamblador.aComandoCrearDesdeRecurso(UsuarioActual.tiendaId(), recurso);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                ProductoRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/productos")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Lista los productos de la tienda (activos y dados de baja)")
    public ResponseEntity<List<ProductoRecurso>> listar() {
        var productos = consultaServicio.manejar(new ObtenerProductosPorTiendaConsulta(UsuarioActual.tiendaId(), false))
                .stream().map(ProductoRecursoEnsamblador::aRecursoDesdeEntidad).toList();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/api/v1/productos/{productoId}")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Obtiene un producto de la tienda")
    public ResponseEntity<ProductoRecurso> obtener(@PathVariable Long productoId) {
        return consultaServicio.manejar(new ObtenerProductoPorIdConsulta(UsuarioActual.tiendaId(), productoId))
                .map(p -> ResponseEntity.ok(ProductoRecursoEnsamblador.aRecursoDesdeEntidad(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/api/v1/productos/{productoId}")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Actualiza un producto")
    public ResponseEntity<?> actualizar(@PathVariable Long productoId, @RequestBody GuardarProductoRecurso recurso) {
        var comando = ProductoRecursoEnsamblador.aComandoActualizarDesdeRecurso(UsuarioActual.tiendaId(), productoId, recurso);
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                ProductoRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }

    @PatchMapping("/api/v1/productos/{productoId}/estado")
    @PreAuthorize("hasAuthority('VENDEDOR')")
    @Operation(summary = "Da de baja o reactiva un producto (baja lógica)")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long productoId, @RequestBody CambiarEstadoProductoRecurso recurso) {
        var comando = new CambiarEstadoProductoComando(UsuarioActual.tiendaId(), productoId, recurso.activo());
        return RespuestaEnsamblador.aRespuestaDesdeResultado(comandoServicio.manejar(comando),
                ProductoRecursoEnsamblador::aRecursoDesdeEntidad, HttpStatus.OK);
    }
}
