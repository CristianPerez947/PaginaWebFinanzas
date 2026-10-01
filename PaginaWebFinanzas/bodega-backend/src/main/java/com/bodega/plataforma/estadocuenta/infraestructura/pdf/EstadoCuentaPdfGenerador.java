package com.bodega.plataforma.estadocuenta.infraestructura.pdf;

import com.bodega.plataforma.estadocuenta.dominio.modelo.agregados.EstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.entidades.LineaEstadoCuenta;
import com.bodega.plataforma.estadocuenta.dominio.modelo.objetosvalor.TipoLinea;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Genera el PDF del estado de cuenta: compras ordenadas por fecha, intereses por días y, en un ítem aparte,
 * los intereses por mora.
 */
@Component
public class EstadoCuentaPdfGenerador {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color COLOR_ENCABEZADO = new Color(0x1F, 0x5F, 0x3D);

    /**
     * @param nombreTienda   nombre comercial de la tienda
     * @param nombreCliente  nombre completo del cliente
     * @param dniCliente     DNI del cliente
     */
    public byte[] generar(EstadoCuenta estado, String nombreTienda, String nombreCliente, String dniCliente) {
        var salida = new ByteArrayOutputStream();
        var documento = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        var titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_ENCABEZADO);
        var negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        var normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        var pequena = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.DARK_GRAY);

        documento.add(new Paragraph(nombreTienda, titulo));
        documento.add(new Paragraph("Estado de cuenta", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
        documento.add(new Paragraph(" ", normal));
        documento.add(new Paragraph("Cliente: %s (DNI %s)".formatted(nombreCliente, dniCliente), normal));
        documento.add(new Paragraph("Fecha de corte: %s".formatted(estado.getFechaCorte().format(FECHA)), normal));
        documento.add(new Paragraph("Fecha de pago: %s".formatted(estado.getFechaPago().format(FECHA)), negrita));
        documento.add(new Paragraph("Moneda: %s".formatted(estado.getMoneda()), normal));
        documento.add(new Paragraph(" ", normal));

        var tabla = new PdfPTable(new float[]{14, 62, 24});
        tabla.setWidthPercentage(100);
        encabezado(tabla, "Fecha", Element.ALIGN_LEFT);
        encabezado(tabla, "Detalle", Element.ALIGN_LEFT);
        encabezado(tabla, "Monto (%s)".formatted(estado.getMoneda()), Element.ALIGN_RIGHT);
        for (var linea : estado.getLineas()) fila(tabla, linea, normal);
        documento.add(tabla);
        documento.add(new Paragraph(" ", normal));

        var resumen = new PdfPTable(new float[]{70, 30});
        resumen.setWidthPercentage(60);
        resumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
        filaResumen(resumen, "Compras a fin de mes", estado.getTotalCompras().toPlainString(), normal);
        filaResumen(resumen, "Intereses compensatorios", estado.getTotalIntereses().toPlainString(), normal);
        filaResumen(resumen, "Cuotas del plan de pagos", estado.getTotalCuotas().toPlainString(), normal);
        filaResumen(resumen, "Intereses por mora", estado.getTotalMora().toPlainString(), normal);
        filaResumen(resumen, "TOTAL A PAGAR", estado.getTotalAPagar().toPlainString(), negrita);
        documento.add(resumen);

        documento.add(new Paragraph(" ", normal));
        documento.add(new Paragraph("El pago debe realizarse completo en la fecha indicada. Pasada la fecha de pago se "
                + "aplican intereses por mora. Los importes se calculan con base de 360 días. "
                + "Generado el %s.".formatted(estado.getFechaGeneracion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                pequena));
        documento.close();
        return salida.toByteArray();
    }

    private void encabezado(PdfPTable tabla, String texto, int alineacion) {
        var fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        var celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(COLOR_ENCABEZADO);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5);
        tabla.addCell(celda);
    }

    private void fila(PdfPTable tabla, LineaEstadoCuenta linea, Font fuente) {
        var esMora = linea.getTipo() == TipoLinea.MORA;
        var fuenteFila = esMora ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(0xB0, 0x24, 0x18)) : fuente;
        tabla.addCell(celda(linea.getFecha().format(FECHA), fuenteFila, Element.ALIGN_LEFT));
        tabla.addCell(celda(linea.getDescripcion(), fuenteFila, Element.ALIGN_LEFT));
        var monto = linea.isIncluidaEnTotal() ? linea.getMonto().toPlainString() : "(informativo) " + linea.getMonto().toPlainString();
        tabla.addCell(celda(monto, fuenteFila, Element.ALIGN_RIGHT));
    }

    private void filaResumen(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        tabla.addCell(celda(etiqueta, fuente, Element.ALIGN_LEFT));
        tabla.addCell(celda(valor, fuente, Element.ALIGN_RIGHT));
    }

    private PdfPCell celda(String texto, Font fuente, int alineacion) {
        var celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(4);
        celda.setBorderColor(Color.LIGHT_GRAY);
        return celda;
    }
}
