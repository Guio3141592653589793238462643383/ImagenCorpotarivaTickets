package com.austral.back.services.admin;

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
import java.util.stream.Collectors;

@Service
public class AdminReportService {

    private final TicketService ticketService;
    private final AdminUsuarioService adminUsuarioService;
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


    public AdminReportService(TicketService ticketService, AdminUsuarioService adminUsuarioService) {
        this.ticketService = ticketService;
        this.adminUsuarioService = adminUsuarioService;
    }

    /**
     * Genera y exporta el reporte mensual en formato Excel
     */
    public void exportarReporteMensual(int year, int month, HttpServletResponse response) throws IOException {
        List<Ticket> tickets = Optional.ofNullable(ticketService.obtenerTodosOrdenados())
                .orElse(Collections.emptyList());
        List<Usuario> usuarios = Optional.ofNullable(adminUsuarioService.obtenerTodos())
                .orElse(Collections.emptyList());

        List<Ticket> filtrados = tickets.stream()
                .filter(t -> t.getMarcaTemporal() != null &&
                        t.getMarcaTemporal().getYear() == year &&
                        t.getMarcaTemporal().getMonthValue() == month)
                .toList();

        Workbook workbook = new XSSFWorkbook();

        // Crear estilos
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle titleStyle = crearEstiloTitulo(workbook);
        CellStyle normalBold = crearEstiloNormalBold(workbook);

        // Hoja 1: Resumen Mensual
        crearHojaResumen(workbook, filtrados, usuarios, year, month, headerStyle, titleStyle, normalBold);

        // Hoja 2: Detalle de Tickets
        crearHojaDetalleTickets(workbook, filtrados, headerStyle);


        // Configurar respuesta para descarga
        configurarRespuestaDescarga(response, year, month, workbook);

        workbook.close();
    }

    private void crearHojaResumen(Workbook workbook, List<Ticket> filtrados, List<Usuario> usuarios,
                                  int year, int month, CellStyle headerStyle, CellStyle titleStyle,
                                  CellStyle normalBold) {
        Sheet resumen = workbook.createSheet("Resumen_Mensual");

        // Título
        Row titleRow = resumen.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE MENSUAL - " + month + "/" + year);
        titleCell.setCellStyle(titleStyle);

        // Estadísticas seguras
        long total = filtrados.size();
        long cerrados = filtrados.stream()
                .filter(t -> "CERRADO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse("")))
                .count();
        long abiertos = filtrados.stream()
                .filter(t -> "ABIERTO".equalsIgnoreCase(Optional.ofNullable(t.getEstado()).orElse("")))
                .count();
        long enProceso = filtrados.stream()
                .filter(t -> "EN_PROCESO".equalsIgnoreCase(
                        Optional.ofNullable(t.getEstado()).orElse("")
                ))
                .count();



        // Datos del resumen
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

        resumen.autoSizeColumn(0);
        resumen.autoSizeColumn(1);
    }

    private void crearHojaDetalleTickets(Workbook workbook, List<Ticket> filtrados, CellStyle headerStyle) {
        Sheet detalle = workbook.createSheet("Detalle_Tickets");
        Row header = detalle.createRow(0);

        String[] headers = {
                "ID", "Fecha", "Nombre Completo", "Correo", "Cargo", "Punto", "Teléfono",
                "Tema", "Descripción", "Estado", "Prioridad", "Atendido por"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int rowDetalle = 1;

        for (Ticket t : filtrados) {
            Row row = detalle.createRow(rowDetalle++);
            row.createCell(0).setCellValue(Optional.ofNullable(t.getId()).orElse(0));
            row.createCell(1).setCellValue(t.getMarcaTemporal() != null ? t.getMarcaTemporal().format(formatter) : "");
            row.createCell(2).setCellValue(Optional.ofNullable(t.getNombreActual()).orElse(""));
            row.createCell(3).setCellValue(Optional.ofNullable(t.getEmailActual()).orElse(""));
            row.createCell(4).setCellValue(t.getUsuario() != null ? Optional.ofNullable(t.getUsuario().getCargo()).orElse("") : "");
            row.createCell(5).setCellValue(t.getUsuario() != null ? Optional.ofNullable(t.getUsuario().getPunto()).orElse("") : "");
            row.createCell(6).setCellValue(Optional.ofNullable(t.getTelefonoActual()).orElse(""));
            row.createCell(7).setCellValue(Optional.ofNullable(t.getTema()).orElse(""));
            row.createCell(8).setCellValue(Optional.ofNullable(t.getDescription()).orElse(""));
            row.createCell(9).setCellValue(Optional.ofNullable(t.getEstado()).orElse(""));
            row.createCell(10).setCellValue(Optional.ofNullable(t.getPriority()).orElse(""));
            row.createCell(11).setCellValue(Optional.ofNullable(t.getAtendidoPor()).orElse(""));
        }

        // 👉 Ajuste de ancho al final
        for (int i = 0; i < headers.length; i++) {
            detalle.autoSizeColumn(i);
            if (detalle.getColumnWidth(i) < 3000) {
                detalle.setColumnWidth(i, 3000); // ancho mínimo (~15 caracteres)
            }
        }
    }




    private void configurarRespuestaDescarga(HttpServletResponse response, int year, int month, Workbook workbook) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=reporte_" + year + "_" + month + ".xlsx");
        workbook.write(response.getOutputStream());
    }
}
