package com.austral.back.services.admin;

import com.austral.back.model.SolicitudMaterial;
import com.austral.back.model.SolicitudMaterialItem;
import com.austral.back.model.SolicitudProductoItem;
import com.austral.back.model.Ticket;
import com.austral.back.model.Usuario;
import com.austral.back.services.ticket.TicketService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminReportService {

    private static final List<String> PRODUCTOS_CATALOGO = List.of(
            "Hev Tech", "Blue Balance", "Sunbalance", "Next", "ColorMatic 3 BIG",
            "Norm - Rodenstock", "VIVIT", "Neochromes", "Nupolar", "Transitions",
            "Myohelp", "Mycon (Rodenstock)", "Polarmax", "Antirreflejos",
            "Soluciones Digitales", "Familia Steady"
    );

    private final TicketService ticketService;
    private final AdminUsuarioService adminUsuarioService;

    public AdminReportService(TicketService ticketService, AdminUsuarioService adminUsuarioService) {
        this.ticketService = ticketService;
        this.adminUsuarioService = adminUsuarioService;
    }

    public void exportarReporteMensual(int year, int month, HttpServletResponse response) throws IOException {
        List<Ticket> tickets = Optional.ofNullable(ticketService.obtenerTodosOrdenados())
                .orElse(Collections.emptyList());
        List<Usuario> usuarios = Optional.ofNullable(adminUsuarioService.obtenerTodos())
                .orElse(Collections.emptyList());

        List<Ticket> filtrados = tickets.stream()
                .filter(t -> t.getMarcaTemporal() != null
                        && t.getMarcaTemporal().getYear() == year
                        && t.getMarcaTemporal().getMonthValue() == month)
                .toList();

        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle titleStyle = crearEstiloTitulo(workbook);
        CellStyle normalBold = crearEstiloNormalBold(workbook);
        CellStyle personStyle = crearEstiloFilaPersona(workbook);

        crearHojaResumen(workbook, filtrados, usuarios, year, month, headerStyle, titleStyle, normalBold);
        crearHojaDetalleTickets(workbook, filtrados, headerStyle);
        crearHojaSolicitudesMaterial(workbook, filtrados, headerStyle, titleStyle, personStyle);

        configurarRespuestaDescarga(response, year, month, workbook);
        workbook.close();
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        return headerStyle;
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        return titleStyle;
    }

    private CellStyle crearEstiloNormalBold(Workbook workbook) {
        CellStyle normalBold = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        normalBold.setFont(boldFont);
        return normalBold;
    }

    private CellStyle crearEstiloFilaPersona(Workbook workbook) {
        CellStyle personStyle = workbook.createCellStyle();
        personStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        personStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        personStyle.setBorderBottom(BorderStyle.THIN);
        personStyle.setBorderTop(BorderStyle.THIN);
        personStyle.setBorderLeft(BorderStyle.THIN);
        personStyle.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        personStyle.setFont(font);
        return personStyle;
    }

    private void crearHojaResumen(Workbook workbook,
                                  List<Ticket> filtrados,
                                  List<Usuario> usuarios,
                                  int year,
                                  int month,
                                  CellStyle headerStyle,
                                  CellStyle titleStyle,
                                  CellStyle normalBold) {
        Sheet resumen = workbook.createSheet("Resumen_Mensual");

        Row titleRow = resumen.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE MENSUAL - " + month + "/" + year);
        titleCell.setCellStyle(titleStyle);

        long total = filtrados.size();
        long cerrados = filtrados.stream()
                .filter(t -> "CERRADO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse("")))
                .count();
        long abiertos = filtrados.stream()
                .filter(t -> "ABIERTO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse("")))
                .count();
        long enProceso = filtrados.stream()
                .filter(t -> "EN_PROCESO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse(""))
                        || "EN PROGRESO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse("")))
                .count();

        String[][] resumenData = {
                {"Total Tickets", String.valueOf(total)},
                {"Cerrados", String.valueOf(cerrados)},
                {"Abiertos", String.valueOf(abiertos)},
                {"En Proceso", String.valueOf(enProceso)},
                {"Total Usuarios", String.valueOf(usuarios.size())},
        };

        int rowNum = 2;
        for (String[] rowData : resumenData) {
            Row row = resumen.createRow(rowNum++);
            Cell label = row.createCell(0);
            label.setCellValue(rowData[0]);
            label.setCellStyle(normalBold);
            row.createCell(1).setCellValue(rowData[1]);
        }

        rowNum += 2;
        Row productosTitulo = resumen.createRow(rowNum++);
        Cell productosCell = productosTitulo.createCell(0);
        productosCell.setCellValue("PRODUCTOS MAS PEDIDOS");
        productosCell.setCellStyle(titleStyle);

        rowNum = crearHeader(resumen, rowNum, headerStyle, "Producto", "Cantidad total");
        for (Map.Entry<String, Integer> entry : calcularTotalesProductos(filtrados).entrySet()) {
            Row row = resumen.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        for (int i = 0; i < 2; i++) {
            resumen.autoSizeColumn(i);
            if (resumen.getColumnWidth(i) < 3000) {
                resumen.setColumnWidth(i, 3000);
            }
        }
    }

    private void crearHojaDetalleTickets(Workbook workbook, List<Ticket> filtrados, CellStyle headerStyle) {
        Sheet detalle = workbook.createSheet("Detalle_Tickets");
        crearHeader(detalle, 0, headerStyle,
                "ID", "Fecha", "Nombre Completo", "Correo", "Cargo", "Punto", "Telefono",
                "Tema", "Descripcion", "Estado", "Prioridad", "Atendido por");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int rowDetalle = 1;

        for (Ticket t : filtrados) {
            Row row = detalle.createRow(rowDetalle++);
            row.createCell(0).setCellValue(Optional.ofNullable(t.getId()).orElse(0));
            row.createCell(1).setCellValue(t.getMarcaTemporal() != null ? t.getMarcaTemporal().format(formatter) : "");
            row.createCell(2).setCellValue(Optional.ofNullable(t.getNombreActual()).orElse(""));
            row.createCell(3).setCellValue(Optional.ofNullable(t.getEmailActual()).orElse(""));
            row.createCell(4).setCellValue(Optional.ofNullable(t.getCargoActual()).orElse(""));
            row.createCell(5).setCellValue(Optional.ofNullable(t.getPuntoActual()).orElse(""));
            row.createCell(6).setCellValue(Optional.ofNullable(t.getTelefonoActual()).orElse(""));
            row.createCell(7).setCellValue(Optional.ofNullable(t.getTema()).orElse(""));
            row.createCell(8).setCellValue(Optional.ofNullable(t.getDescription()).orElse(""));
            row.createCell(9).setCellValue(Optional.ofNullable(t.getEstado()).orElse(""));
            row.createCell(10).setCellValue(Optional.ofNullable(t.getPriority()).orElse(""));
            row.createCell(11).setCellValue(Optional.ofNullable(t.getAtendidoPor()).orElse(""));
        }

        ajustarColumnas(detalle, 12);
    }

    private void crearHojaSolicitudesMaterial(Workbook workbook,
                                              List<Ticket> filtrados,
                                              CellStyle headerStyle,
                                              CellStyle titleStyle,
                                              CellStyle personStyle) {
        Sheet hoja = workbook.createSheet("Solicitudes_Detalle");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int rowNum = 0;

        rowNum = crearTituloTabla(hoja, rowNum, "Solicitudes", titleStyle);
        rowNum = crearHeader(hoja, rowNum, headerStyle, "id", "persona", "fecha", "estado");
        for (Ticket t : filtrados) {
            Row row = hoja.createRow(rowNum++);
            row.createCell(0).setCellValue(Optional.ofNullable(t.getId()).orElse(0));
            row.createCell(1).setCellValue(Optional.ofNullable(t.getNombreActual()).orElse(""));
            row.createCell(2).setCellValue(t.getMarcaTemporal() != null ? t.getMarcaTemporal().format(formatter) : "");
            row.createCell(3).setCellValue(Optional.ofNullable(t.getEstado()).orElse(""));
            for (int i = 0; i < 4; i++) {
                row.getCell(i).setCellStyle(personStyle);
            }
        }

        rowNum += 2;
        rowNum = crearTituloTabla(hoja, rowNum, "Productos por solicitud", titleStyle);
        rowNum = crearHeader(hoja, rowNum, headerStyle, "id", "solicitud_id", "producto_id", "cantidad");
        int productoRowId = 1;
        for (Ticket t : filtrados) {
            SolicitudMaterial solicitud = t.getSolicitudMaterial();
            if (solicitud == null) {
                continue;
            }

            for (SolicitudProductoItem item : obtenerProductosReporte(solicitud)) {
                Row row = hoja.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getId() != null ? item.getId() : productoRowId);
                row.createCell(1).setCellValue(solicitud.getId() != null ? solicitud.getId() : 0);
                row.createCell(2).setCellValue(Optional.ofNullable(item.getProductoId()).orElse(""));
                row.createCell(3).setCellValue(Optional.ofNullable(item.getCantidad()).orElse(0));
                productoRowId++;
            }
        }

        rowNum += 2;
        rowNum = crearTituloTabla(hoja, rowNum, "Materiales por solicitud", titleStyle);
        rowNum = crearHeader(hoja, rowNum, headerStyle, "id", "solicitud_id", "material_id", "cantidad");
        int materialRowId = 1;
        for (Ticket t : filtrados) {
            SolicitudMaterial solicitud = t.getSolicitudMaterial();
            if (solicitud == null) {
                continue;
            }

            for (SolicitudMaterialItem item : obtenerMaterialesReporte(solicitud)) {
                Row row = hoja.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getId() != null ? item.getId() : materialRowId);
                row.createCell(1).setCellValue(solicitud.getId() != null ? solicitud.getId() : 0);
                row.createCell(2).setCellValue(Optional.ofNullable(item.getMaterialId()).orElse(""));
                row.createCell(3).setCellValue(Optional.ofNullable(item.getCantidad()).orElse(0));
                materialRowId++;
            }
        }

        ajustarColumnas(hoja, 4);
    }

    private int crearTituloTabla(Sheet hoja, int rowNum, String titulo, CellStyle titleStyle) {
        Row row = hoja.createRow(rowNum++);
        Cell cell = row.createCell(0);
        cell.setCellValue(titulo);
        cell.setCellStyle(titleStyle);
        return rowNum;
    }

    private int crearHeader(Sheet hoja, int rowNum, CellStyle headerStyle, String... headers) {
        Row row = hoja.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        return rowNum;
    }

    private void ajustarColumnas(Sheet hoja, int cantidadColumnas) {
        for (int i = 0; i < cantidadColumnas; i++) {
            hoja.autoSizeColumn(i);
            if (hoja.getColumnWidth(i) < 3000) {
                hoja.setColumnWidth(i, 3000);
            }
        }
    }

    private Map<String, Integer> calcularTotalesProductos(List<Ticket> tickets) {
        Map<String, Integer> totales = new LinkedHashMap<>();
        PRODUCTOS_CATALOGO.forEach(producto -> totales.put(producto, 0));

        for (Ticket ticket : tickets) {
            SolicitudMaterial solicitud = ticket.getSolicitudMaterial();
            if (solicitud == null) {
                continue;
            }

            for (SolicitudProductoItem item : obtenerProductosReporte(solicitud)) {
                String producto = Optional.ofNullable(item.getProductoId()).orElse("").trim();
                if (producto.isEmpty()) {
                    continue;
                }
                int cantidad = Optional.ofNullable(item.getCantidad()).orElse(0);
                totales.merge(producto, cantidad, Integer::sum);
            }
        }

        return totales;
    }

    private List<SolicitudProductoItem> obtenerProductosReporte(SolicitudMaterial solicitud) {
        if (solicitud.getProductos() != null && !solicitud.getProductos().isEmpty()) {
            return solicitud.getProductos();
        }

        if (solicitud.getProducto() == null || solicitud.getProducto().isBlank()) {
            return Collections.emptyList();
        }

        SolicitudProductoItem item = new SolicitudProductoItem();
        item.setProductoId(solicitud.getProducto());
        item.setCantidad(solicitud.getCantidad() != null ? solicitud.getCantidad() : 1);
        return List.of(item);
    }

    private List<SolicitudMaterialItem> obtenerMaterialesReporte(SolicitudMaterial solicitud) {
        if (solicitud.getMateriales() != null && !solicitud.getMateriales().isEmpty()) {
            return solicitud.getMateriales();
        }

        List<SolicitudMaterialItem> materiales = new ArrayList<>();
        agregarMaterialReporte(materiales, solicitud.isAyudaventasImpresos(), "Ayudaventas Impresos");
        agregarMaterialReporte(materiales, solicitud.isListasDePrecios(), "Listas de Precios");
        agregarMaterialReporte(materiales, solicitud.isMuestrasLentes(), "Muestras de Lentes");
        agregarMaterialReporte(materiales, solicitud.isRegaloCorporativo(), "Regalos Corporativos");
        agregarMaterialReporte(materiales, solicitud.isMaterialCapacitaciones(), "Material Capacitaciones");
        agregarMaterialReporte(materiales, solicitud.isPaniosMarcados(), "Panos Marcados");
        agregarMaterialReporte(materiales, solicitud.isLibretaNotas(), "Libreta de Notas");
        agregarMaterialReporte(materiales, solicitud.isReglillas(), "Reglillas");
        agregarMaterialReporte(materiales, solicitud.isVideosUsb(), "Videos - USB");
        agregarMaterialReporte(materiales, solicitud.isEsferos(), "Esferos");
        agregarMaterialReporte(materiales, solicitud.isHabladores(), "Habladores");
        return materiales;
    }

    private void agregarMaterialReporte(List<SolicitudMaterialItem> materiales, boolean seleccionado, String materialId) {
        if (!seleccionado) {
            return;
        }

        SolicitudMaterialItem item = new SolicitudMaterialItem();
        item.setMaterialId(materialId);
        item.setCantidad(1);
        materiales.add(item);
    }

    private void configurarRespuestaDescarga(HttpServletResponse response, int year, int month, Workbook workbook) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=reporte_" + year + "_" + month + ".xlsx");
        workbook.write(response.getOutputStream());
    }
}
