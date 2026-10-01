package com.bodega.plataforma.semilla;

import com.bodega.plataforma.catalogo.aplicacion.comandoservicios.ProductoComandoServicio;
import com.bodega.plataforma.catalogo.dominio.modelo.agregados.Producto;
import com.bodega.plataforma.catalogo.dominio.modelo.comandos.CrearProductoComando;
import com.bodega.plataforma.clientes.aplicacion.comandoservicios.ClienteComandoServicio;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CambiarEstadoClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.comandos.CrearClienteComando;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Capitalizacion;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.CondicionesCredito;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.Moneda;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.TasaInteres;
import com.bodega.plataforma.clientes.dominio.modelo.objetosvalor.TipoTasa;
import com.bodega.plataforma.compartido.aplicacion.resultado.ErrorAplicacion;
import com.bodega.plataforma.compartido.aplicacion.resultado.Resultado;
import com.bodega.plataforma.compartido.infraestructura.reloj.RelojSistemaImpl;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.DeudaComandoServicio;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.VentaCreditoComandoServicio;
import com.bodega.plataforma.credito.aplicacion.comandoservicios.VentaCreditoComandoServicio.VentaConstruida;
import com.bodega.plataforma.credito.dominio.modelo.agregados.Obligacion;
import com.bodega.plataforma.credito.dominio.modelo.comandos.CondonarObligacionComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCicloComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarPagoCuotaComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarVentaComando;
import com.bodega.plataforma.credito.dominio.modelo.comandos.RegistrarVentaComando.LineaVentaComando;
import com.bodega.plataforma.credito.dominio.modelo.objetosvalor.Modalidad;
import com.bodega.plataforma.estadocuenta.aplicacion.interno.tareas.CorteAutomaticoTarea;
import com.bodega.plataforma.identidad.interfaces.acl.IdentidadContextoFachada;
import com.bodega.plataforma.tiendas.aplicacion.comandoservicios.TiendaComandoServicio;
import com.bodega.plataforma.tiendas.aplicacion.consultaservicios.TiendaConsultaServicio;
import com.bodega.plataforma.tiendas.dominio.modelo.comandos.CrearTiendaComando;
import com.bodega.plataforma.tiendas.dominio.modelo.consultas.ObtenerTodasLasTiendasConsulta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Datos de demostración: dos bodegas con sus productos, clientes y un historial de ventas, pagos, mora y
 * condonaciones construido con la fecha del sistema (se simula el reloj hacia atrás y luego se restaura).
 * Solo se ejecuta si no existe ninguna tienda y la propiedad semilla.datos-demo está activa.
 * <p>
 * Credenciales de demostración: vendedor de Don Pepe = pepe / pepe123; vendedor de Doña Rosa = rosa / rosa123;
 * todos los clientes de demostración tienen la clave 123456.
 */
@Slf4j
@Component
public class SemillaDemostracion {

    private static final String CLAVE_CLIENTES_DEMO = "123456";

    private final boolean activa;
    private final TiendaComandoServicio tiendaComando;
    private final TiendaConsultaServicio tiendaConsulta;
    private final ProductoComandoServicio productoComando;
    private final ClienteComandoServicio clienteComando;
    private final VentaCreditoComandoServicio ventaComando;
    private final DeudaComandoServicio deudaComando;
    private final IdentidadContextoFachada identidadFachada;
    private final CorteAutomaticoTarea corteTarea;
    private final RelojSistemaImpl reloj;

    public SemillaDemostracion(@Value("${semilla.datos-demo}") boolean activa, TiendaComandoServicio tiendaComando,
                               TiendaConsultaServicio tiendaConsulta, ProductoComandoServicio productoComando,
                               ClienteComandoServicio clienteComando, VentaCreditoComandoServicio ventaComando,
                               DeudaComandoServicio deudaComando, IdentidadContextoFachada identidadFachada,
                               CorteAutomaticoTarea corteTarea, RelojSistemaImpl reloj) {
        this.activa = activa;
        this.tiendaComando = tiendaComando;
        this.tiendaConsulta = tiendaConsulta;
        this.productoComando = productoComando;
        this.clienteComando = clienteComando;
        this.ventaComando = ventaComando;
        this.deudaComando = deudaComando;
        this.identidadFachada = identidadFachada;
        this.corteTarea = corteTarea;
        this.reloj = reloj;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        if (!activa || !tiendaConsulta.manejar(new ObtenerTodasLasTiendasConsulta(false)).isEmpty()) return;
        var hoy = reloj.hoy();
        try {
            log.info("Cargando datos de demostración...");
            cargarDonPepe(hoy);
            cargarDonaRosa(hoy);
            reloj.restaurarRelojReal();
            for (var tienda : tiendaConsulta.manejar(new ObtenerTodasLasTiendasConsulta(true)))
                corteTarea.procesarTienda(tienda.getId());
            log.info("Datos de demostración cargados. Vendedores: pepe/pepe123 y rosa/rosa123. Clientes: clave {}.",
                    CLAVE_CLIENTES_DEMO);
        } catch (Exception e) {
            log.error("No se pudieron cargar los datos de demostración: {}", e.getMessage(), e);
        } finally {
            reloj.restaurarRelojReal();
        }
    }

    // ------------------------------------------------------------------------------------------------
    // La Tiendita de Don Pepe
    // ------------------------------------------------------------------------------------------------

    private void cargarDonPepe(LocalDate hoy) {
        var tiendaId = crearTienda("La Tiendita de Don Pepe", "tiendita-don-pepe", "logo-don-pepe.png", "20512345678",
                "Av. Los Olivos 123, Los Olivos, Lima", "987654321", "José Pérez Ramírez", "pepe", "pepe123");
        var p = productosDonPepe(tiendaId);

        var maria = cliente(tiendaId, "45871236", "María", "Quispe Rojas", "987111222", "PEN", "500", 3, 20, 26,
                efectiva("24"), efectiva("36"));
        var juan = cliente(tiendaId, "40219876", "Juan", "Huamán Cárdenas", "987222333", "PEN", "800", 6, 20, 26,
                nominal("30", Capitalizacion.MENSUAL), nominal("42", Capitalizacion.MENSUAL));
        var rosa = cliente(tiendaId, "32145698", "Rosa", "Mendoza Silva", "987333444", "PEN", "300", 3, 10, 15,
                efectiva("18"), efectiva("30"));
        var carlos = cliente(tiendaId, "41236547", "Carlos", "Ramírez Ponce", "987444555", "PEN", "600", 4, 25, 5,
                efectiva("20"), efectiva("36"));
        var lucia = cliente(tiendaId, "46587412", "Lucía", "Torres Vega", "987555666", "PEN", "150", 2, 20, 26,
                efectiva("24"), efectiva("36"));
        var pedro = cliente(tiendaId, "43698521", "Pedro", "Salazar Díaz", "987666777", "USD", "200", 3, 20, 26,
                efectiva("15"), efectiva("24"));
        var ana = cliente(tiendaId, "47521369", "Ana", "Flores Núñez", "987777888", "PEN", "400", 3, 15, 22,
                efectiva("24"), efectiva("36"));
        var miguel = cliente(tiendaId, "44789632", "Miguel", "Castro Luna", "987888999", "PEN", "700", 6, 20, 26,
                nominal("24", Capitalizacion.DIARIA), nominal("36", Capitalizacion.DIARIA));
        cliente(tiendaId, "48123654", "Elena", "Vargas Ruiz", "987999000", "PEN", "250", 2, 20, 26,
                efectiva("24"), efectiva("36"));
        var jose = cliente(tiendaId, "42365478", "José", "Paredes Alva", "987000111", "PEN", "300", 3, 20, 26,
                efectiva("24"), efectiva("36"));

        // María: una compra a fin de mes ya pagada a tiempo y otra reciente pendiente.
        var v = vender(tiendaId, maria, hoy.minusDays(75).atTime(10, 0),
                l(p, "Arroz Costeño 1 kg", "2", Modalidad.FIN_DE_MES, null), l(p, "Aceite Primor 1 L", "1", Modalidad.FIN_DE_MES, null),
                l(p, "Leche Gloria Tarro 400 g", "6", Modalidad.FIN_DE_MES, null));
        pagarCiclo(tiendaId, maria, v.obligaciones().get(0), 0);
        vender(tiendaId, maria, hoy.minusDays(6).atTime(11, 0),
                l(p, "Azúcar Rubia 1 kg", "3", Modalidad.FIN_DE_MES, null), l(p, "Pollo entero (kg)", "2", Modalidad.FIN_DE_MES, null),
                l(p, "Huevos Rosados 1 kg", "1", Modalidad.FIN_DE_MES, null));

        // Juan: plan de 3 cuotas: la 1.ª a tiempo, la 2.ª con 9 días de mora y la 3.ª vencida sin pagar.
        v = vender(tiendaId, juan, hoy.minusDays(140).atTime(9, 30),
                l(p, "Canasta básica familiar", "1", Modalidad.CUOTAS, 3), l(p, "Detergente Bolívar 2.6 kg", "1", Modalidad.CUOTAS, 3));
        var plan = v.obligaciones().get(0);
        pagarCuota(tiendaId, juan, plan, 1, plan.getCuotas().get(0).getFechaVencimiento(), hoy);
        pagarCuota(tiendaId, juan, plan, 2, plan.getCuotas().get(1).getFechaVencimiento().plusDays(9), hoy);

        // Rosa: compra a fin de mes sin pagar: con mora.
        vender(tiendaId, rosa, hoy.minusDays(70).atTime(16, 0),
                l(p, "Arroz Costeño 1 kg", "5", Modalidad.FIN_DE_MES, null), l(p, "Aceite Primor 1 L", "2", Modalidad.FIN_DE_MES, null),
                l(p, "Atún Florida Filete 170 g", "6", Modalidad.FIN_DE_MES, null));

        // Carlos: compra después de su corte (día 26) que pasa al ciclo siguiente, y una compra en 2 cuotas.
        var despuesDelCorte = hoy.minusMonths(1).withDayOfMonth(26).atTime(10, 0);
        vender(tiendaId, carlos, despuesDelCorte.isAfter(LocalDateTime.of(hoy, LocalTime.of(0, 0))) ? hoy.minusDays(40).atTime(10, 0) : despuesDelCorte,
                l(p, "Fideos Don Vittorio 500 g", "4", Modalidad.FIN_DE_MES, null), l(p, "Queso fresco 1 kg", "1", Modalidad.FIN_DE_MES, null));
        vender(tiendaId, carlos, hoy.minusDays(30).atTime(15, 0),
                l(p, "Pack útiles escolares", "1", Modalidad.CUOTAS, 2));

        // Lucía: cerca de su límite de S/ 150.00.
        vender(tiendaId, lucia, hoy.minusDays(8).atTime(12, 0),
                l(p, "Pollo entero (kg)", "2", Modalidad.FIN_DE_MES, null), l(p, "Queso fresco 1 kg", "1", Modalidad.FIN_DE_MES, null),
                l(p, "Aceite Primor 1 L", "2", Modalidad.FIN_DE_MES, null), l(p, "Arroz Costeño 1 kg", "3", Modalidad.FIN_DE_MES, null),
                l(p, "Leche Gloria Tarro 400 g", "6", Modalidad.FIN_DE_MES, null), l(p, "Yogurt Gloria 1 L", "2", Modalidad.FIN_DE_MES, null));

        // Pedro: crédito en dólares (tipo de cambio 3.75).
        vender(tiendaId, pedro, hoy.minusDays(15).atTime(10, 30),
                l(p, "Balón de gas 10 kg", "1", Modalidad.FIN_DE_MES, null), l(p, "Pack útiles escolares", "1", Modalidad.CUOTAS, 2));

        // Ana: una deuda condonada y otra pagada a tiempo.
        v = vender(tiendaId, ana, hoy.minusDays(60).atTime(14, 0),
                l(p, "Detergente Bolívar 2.6 kg", "1", Modalidad.FIN_DE_MES, null), l(p, "Papel higiénico Elite x4", "2", Modalidad.FIN_DE_MES, null));
        condonar(tiendaId, v.obligaciones().get(0), hoy.minusDays(50), "Ayuda social por emergencia familiar");
        v = vender(tiendaId, ana, hoy.minusDays(35).atTime(9, 0),
                l(p, "Inca Kola 1.5 L", "2", Modalidad.FIN_DE_MES, null), l(p, "Galletas Soda Field x6", "3", Modalidad.FIN_DE_MES, null));
        pagarCiclo(tiendaId, ana, v.obligaciones().get(0), 0);

        // Miguel: plan de 4 cuotas al día, con tasa nominal capitalizable diariamente.
        vender(tiendaId, miguel, hoy.minusDays(20).atTime(10, 0),
                l(p, "Canasta básica familiar", "1", Modalidad.CUOTAS, 4), l(p, "Balón de gas 10 kg", "1", Modalidad.CUOTAS, 4));

        // José: compra pagada y luego dado de baja.
        v = vender(tiendaId, jose, hoy.minusDays(90).atTime(10, 0), l(p, "Arroz Costeño 1 kg", "4", Modalidad.FIN_DE_MES, null));
        pagarCiclo(tiendaId, jose, v.obligaciones().get(0), 0);
        reloj.restaurarRelojReal();
        esperar(clienteComando.manejar(new CambiarEstadoClienteComando(tiendaId, jose, false)));
    }

    // ------------------------------------------------------------------------------------------------
    // Bodega Doña Rosa
    // ------------------------------------------------------------------------------------------------

    private void cargarDonaRosa(LocalDate hoy) {
        var tiendaId = crearTienda("Bodega Doña Rosa", "bodega-dona-rosa", "logo-dona-rosa.png", "20698765432",
                "Jr. Los Pinos 456, San Martín de Porres, Lima", "955123456", "Rosa Campos Díaz", "rosa", "rosa123");
        var p = productosDonaRosa(tiendaId);
        var teresa = cliente(tiendaId, "40112233", "Teresa", "Campos Lima", "955000111", "PEN", "300", 3, 18, 25,
                efectiva("22"), efectiva("34"));
        var victor = cliente(tiendaId, "41223344", "Víctor", "León Mora", "955000222", "PEN", "500", 4, 18, 25,
                efectiva("22"), efectiva("34"));
        vender(tiendaId, teresa, hoy.minusDays(10).atTime(11, 0),
                l(p, "Arroz Costeño 1 kg", "3", Modalidad.FIN_DE_MES, null), l(p, "Leche Gloria Tarro 400 g", "4", Modalidad.FIN_DE_MES, null));
        vender(tiendaId, victor, hoy.minusDays(30).atTime(17, 0),
                l(p, "Canasta básica familiar", "1", Modalidad.CUOTAS, 3));
    }

    // ------------------------------------------------------------------------------------------------
    // Catálogos
    // ------------------------------------------------------------------------------------------------

    private Map<String, Long> productosDonPepe(Long tiendaId) {
        var ids = new HashMap<String, Long>();
        producto(ids, tiendaId, "Arroz Costeño 1 kg", "Abarrotes", "Costeño", "Alicorp", "kg", "arroz-costeno.jpg", "4.20", "4.20", false);
        producto(ids, tiendaId, "Azúcar Rubia 1 kg", "Abarrotes", "Cartavio", "Cartavio", "kg", "azucar-rubia.jpg", "3.90", "3.90", false);
        producto(ids, tiendaId, "Aceite Primor 1 L", "Abarrotes", "Primor", "Alicorp", "litro", "aceite-primor.jpg", "9.50", "9.50", false);
        producto(ids, tiendaId, "Fideos Don Vittorio 500 g", "Abarrotes", "Don Vittorio", "Alicorp", "paquete", "fideos-don-vittorio.jpg", "3.20", "3.20", false);
        producto(ids, tiendaId, "Lentejas 500 g", "Abarrotes", "Bells", "Bells", "paquete", "lentejas.jpg", "4.80", "4.80", false);
        producto(ids, tiendaId, "Avena 3 Ositos 250 g", "Abarrotes", "3 Ositos", "Nestlé", "paquete", "avena-3-ositos.jpg", "2.80", "2.80", false);
        producto(ids, tiendaId, "Atún Florida Filete 170 g", "Conservas", "Florida", "Florida", "lata", "atun-florida.jpg", "5.50", "5.50", false);
        producto(ids, tiendaId, "Leche Gloria Tarro 400 g", "Lácteos", "Gloria", "Gloria", "tarro", "leche-gloria.jpg", "4.30", "4.30", false);
        producto(ids, tiendaId, "Yogurt Gloria 1 L", "Lácteos", "Gloria", "Gloria", "botella", "yogurt-gloria.jpg", "7.20", "7.20", false);
        producto(ids, tiendaId, "Queso fresco 1 kg", "Lácteos", "Artesanal", "Granja local", "kg", "queso-fresco.jpg", "26.00", "27.00", false);
        producto(ids, tiendaId, "Huevos Rosados 1 kg", "Frescos", "Rosados", "Granja Rosado", "kg", "huevos-rosados.jpg", "8.50", "8.50", false);
        producto(ids, tiendaId, "Pollo entero (kg)", "Frescos", "San Fernando", "San Fernando", "kg", "pollo-entero.jpg", "12.50", "12.50", false);
        producto(ids, tiendaId, "Pan francés (10 unidades)", "Panadería", "Panadería del barrio", "Panadería del barrio", "bolsa", "pan-frances.jpg", "4.00", "4.00", false);
        producto(ids, tiendaId, "Inca Kola 1.5 L", "Bebidas", "Inca Kola", "Coca-Cola", "botella", "inca-kola-1-5l.jpg", "6.50", "6.50", false);
        producto(ids, tiendaId, "Coca-Cola 2 L", "Bebidas", "Coca-Cola", "Coca-Cola", "botella", "coca-cola-2l.jpg", "8.50", "8.50", false);
        producto(ids, tiendaId, "Agua San Luis 625 ml", "Bebidas", "San Luis", "Backus", "botella", "agua-san-luis.jpg", "1.50", "1.50", false);
        producto(ids, tiendaId, "Cerveza Cristal 620 ml", "Licores", "Cristal", "Backus", "botella", "cerveza-cristal.jpg", "6.00", "6.00", false);
        producto(ids, tiendaId, "Galletas Soda Field x6", "Snacks", "Field", "Field", "paquete", "galletas-soda-field.jpg", "3.60", "3.60", false);
        producto(ids, tiendaId, "Papas Lays Clásicas 150 g", "Snacks", "Lays", "Frito Lay", "bolsa", "papas-lays.jpg", "6.50", "6.50", false);
        producto(ids, tiendaId, "Chocolate Sublime", "Snacks", "Sublime", "Nestlé", "unidad", "chocolate-sublime.jpg", "1.80", "1.80", false);
        producto(ids, tiendaId, "Detergente Bolívar 2.6 kg", "Limpieza", "Bolívar", "Alicorp", "bolsa", "detergente-bolivar.jpg", "24.90", "26.00", true);
        producto(ids, tiendaId, "Lejía Clorox 1 L", "Limpieza", "Clorox", "Clorox", "botella", "lejia-clorox.jpg", "4.50", "4.50", false);
        producto(ids, tiendaId, "Jabón Bolívar barra", "Limpieza", "Bolívar", "Alicorp", "unidad", "jabon-bolivar.jpg", "2.20", "2.20", false);
        producto(ids, tiendaId, "Papel higiénico Elite x4", "Higiene", "Elite", "Kimberly-Clark", "paquete", "papel-higienico-elite.jpg", "6.80", "6.80", false);
        producto(ids, tiendaId, "Shampoo Head & Shoulders 375 ml", "Higiene", "Head & Shoulders", "P&G", "frasco", "shampoo-head-shoulders.jpg", "18.90", "19.90", true);
        producto(ids, tiendaId, "Pasta dental Colgate 75 ml", "Higiene", "Colgate", "Colgate-Palmolive", "tubo", "pasta-dental-colgate.jpg", "5.90", "5.90", false);
        producto(ids, tiendaId, "Recarga de agua bidón 20 L", "Bebidas", "Agua de mesa", "Distribuidora local", "bidón", "bidon-agua.jpg", "9.00", "9.00", false);
        producto(ids, tiendaId, "Balón de gas 10 kg", "Gas", "Solgas", "Solgas", "balón", "balon-gas.jpg", "58.00", "60.00", true);
        producto(ids, tiendaId, "Canasta básica familiar", "Combos", "Combo Don Pepe", "Don Pepe", "canasta", "canasta-basica.jpg", "120.00", "125.00", true);
        producto(ids, tiendaId, "Pack útiles escolares", "Bazar", "Faber-Castell", "Distribuidora escolar", "pack", "pack-utiles.jpg", "45.00", "48.00", true);
        return ids;
    }

    private Map<String, Long> productosDonaRosa(Long tiendaId) {
        var ids = new HashMap<String, Long>();
        producto(ids, tiendaId, "Arroz Costeño 1 kg", "Abarrotes", "Costeño", "Alicorp", "kg", "arroz-costeno.jpg", "4.30", "4.30", false);
        producto(ids, tiendaId, "Azúcar Rubia 1 kg", "Abarrotes", "Cartavio", "Cartavio", "kg", "azucar-rubia.jpg", "4.00", "4.00", false);
        producto(ids, tiendaId, "Aceite Primor 1 L", "Abarrotes", "Primor", "Alicorp", "litro", "aceite-primor.jpg", "9.80", "9.80", false);
        producto(ids, tiendaId, "Fideos Don Vittorio 500 g", "Abarrotes", "Don Vittorio", "Alicorp", "paquete", "fideos-don-vittorio.jpg", "3.30", "3.30", false);
        producto(ids, tiendaId, "Leche Gloria Tarro 400 g", "Lácteos", "Gloria", "Gloria", "tarro", "leche-gloria.jpg", "4.40", "4.40", false);
        producto(ids, tiendaId, "Huevos Rosados 1 kg", "Frescos", "Rosados", "Granja Rosado", "kg", "huevos-rosados.jpg", "8.80", "8.80", false);
        producto(ids, tiendaId, "Inca Kola 1.5 L", "Bebidas", "Inca Kola", "Coca-Cola", "botella", "inca-kola-1-5l.jpg", "6.60", "6.60", false);
        producto(ids, tiendaId, "Galletas Soda Field x6", "Snacks", "Field", "Field", "paquete", "galletas-soda-field.jpg", "3.70", "3.70", false);
        producto(ids, tiendaId, "Detergente Bolívar 2.6 kg", "Limpieza", "Bolívar", "Alicorp", "bolsa", "detergente-bolivar.jpg", "25.50", "26.50", true);
        producto(ids, tiendaId, "Papel higiénico Elite x4", "Higiene", "Elite", "Kimberly-Clark", "paquete", "papel-higienico-elite.jpg", "7.00", "7.00", false);
        producto(ids, tiendaId, "Balón de gas 10 kg", "Gas", "Solgas", "Solgas", "balón", "balon-gas.jpg", "59.00", "61.00", true);
        producto(ids, tiendaId, "Canasta básica familiar", "Combos", "Combo Doña Rosa", "Doña Rosa", "canasta", "canasta-basica.jpg", "118.00", "123.00", true);
        return ids;
    }

    // ------------------------------------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------------------------------------

    private Long crearTienda(String nombre, String slug, String logo, String ruc, String direccion, String telefono,
                             String dueno, String usuario, String clave) {
        var tienda = esperar(tiendaComando.manejar(new CrearTiendaComando(nombre, slug, logo, ruc, direccion, telefono,
                new BigDecimal("3.7500"), dueno, usuario, clave)));
        return tienda.getId();
    }

    private void producto(Map<String, Long> ids, Long tiendaId, String nombre, String categoria, String marca,
                          String proveedor, String unidad, String imagen, String contado, String credito, boolean cuotas) {
        Producto p = esperar(productoComando.manejar(new CrearProductoComando(tiendaId, nombre, categoria, marca, proveedor,
                nombre, unidad, imagen, new BigDecimal(contado), new BigDecimal(credito), cuotas)));
        ids.put(nombre, p.getId());
    }

    private Long cliente(Long tiendaId, String dni, String nombres, String apellidos, String telefono, String moneda,
                         String limite, int maxMeses, int corte, int pago, TasaInteres compensatoria, TasaInteres mora) {
        var condiciones = new CondicionesCredito(Moneda.valueOf(moneda), new BigDecimal(limite), maxMeses, corte,
                LocalTime.of(23, 59), pago, compensatoria, mora);
        var creado = esperar(clienteComando.manejar(new CrearClienteComando(tiendaId, dni, nombres, apellidos, telefono,
                "Lima", condiciones)));
        identidadFachada.cambiarClave(creado.cliente().getUsuarioId(), CLAVE_CLIENTES_DEMO);
        return creado.cliente().getId();
    }

    private static TasaInteres efectiva(String porcentaje) {
        return new TasaInteres(TipoTasa.EFECTIVA, new BigDecimal(porcentaje), null);
    }

    private static TasaInteres nominal(String porcentaje, Capitalizacion capitalizacion) {
        return new TasaInteres(TipoTasa.NOMINAL, new BigDecimal(porcentaje), capitalizacion);
    }

    private static LineaVentaComando l(Map<String, Long> productos, String nombre, String cantidad, Modalidad modalidad,
                                       Integer cuotas) {
        var id = productos.get(nombre);
        if (id == null) throw new IllegalStateException("Producto de demostración inexistente: " + nombre);
        return new LineaVentaComando(id, new BigDecimal(cantidad), modalidad, cuotas);
    }

    private VentaConstruida vender(Long tiendaId, Long clienteId, LocalDateTime fechaHora, LineaVentaComando... lineas) {
        reloj.simular(fechaHora);
        return esperar(ventaComando.manejar(new RegistrarVentaComando(tiendaId, clienteId, List.of(lineas))));
    }

    private void pagarCiclo(Long tiendaId, Long clienteId, Obligacion obligacion, int diasDespuesDelVencimiento) {
        var fecha = obligacion.getFechaPago().plusDays(diasDespuesDelVencimiento);
        reloj.simular(fecha.atTime(12, 0));
        esperar(deudaComando.manejar(new RegistrarPagoCicloComando(tiendaId, clienteId, obligacion.getFechaPago(), fecha)));
    }

    /** Paga una cuota en la fecha indicada solo si esa fecha ya pasó respecto de hoy. */
    private void pagarCuota(Long tiendaId, Long clienteId, Obligacion plan, int numero, LocalDate fecha, LocalDate hoy) {
        if (fecha.isAfter(hoy)) return;
        reloj.simular(fecha.atTime(12, 0));
        esperar(deudaComando.manejar(new RegistrarPagoCuotaComando(tiendaId, plan.getId(), numero, fecha)));
    }

    private void condonar(Long tiendaId, Obligacion obligacion, LocalDate fecha, String motivo) {
        reloj.simular(fecha.atTime(12, 0));
        esperar(deudaComando.manejar(new CondonarObligacionComando(tiendaId, obligacion.getId(), motivo)));
    }

    private static <T> T esperar(Resultado<T, ErrorAplicacion> resultado) {
        if (resultado instanceof Resultado.Exito<T, ErrorAplicacion> exito) return exito.valor();
        var error = ((Resultado.Fallo<T, ErrorAplicacion>) resultado).error();
        throw new IllegalStateException("%s: %s".formatted(error.mensaje(), error.detalle()));
    }
}
