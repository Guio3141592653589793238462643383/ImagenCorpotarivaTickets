package com.austral.back.services.media;

import com.austral.back.model.TicketMedia;
import com.austral.back.repository.TicketMediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MediaService {

    private final TicketMediaRepository ticketMediaRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final String MEDIA_URL_PREFIX = "/uploads/";

    public MediaService(TicketMediaRepository ticketMediaRepository) {
        this.ticketMediaRepository = ticketMediaRepository;
    }

    /**
     * Obtiene todos los medios de un ticket
     */
    public List<TicketMedia> obtenerMediasPorTicket(Integer ticketId) {
        return ticketMediaRepository.findByTicketId(ticketId);
    }

    /**
     * Retorna la URL accesible de un archivo media
     */
    public String obtenerUrlMedia(String nombreArchivo) {
        return MEDIA_URL_PREFIX + nombreArchivo;
    }

    /**
     * Obtiene la ruta completa del archivo
     */
    public String obtenerRutaCompleta(String nombreArchivo) {
        return uploadDir + nombreArchivo;
    }

    /**
     * Verifica si un archivo es una imagen
     */
    public boolean esImagen(String nombreArchivo) {
        String extensionLower = nombreArchivo.toLowerCase();
        return extensionLower.endsWith(".jpg")
            || extensionLower.endsWith(".jpeg")
            || extensionLower.endsWith(".png")
            || extensionLower.endsWith(".gif")
            || extensionLower.endsWith(".webp")
            || extensionLower.endsWith(".bmp");
    }

    /**
     * Obtiene el tipo de archivo (imagen, documento, etc)
     */
    public String obtenerTipoArchivo(String nombreArchivo) {
        String extensionLower = nombreArchivo.toLowerCase();

        if (esImagen(nombreArchivo)) {
            return "imagen";
        } else if (extensionLower.endsWith(".pdf")) {
            return "pdf";
        } else if (extensionLower.endsWith(".doc") || extensionLower.endsWith(".docx")) {
            return "documento";
        } else if (extensionLower.endsWith(".zip") || extensionLower.endsWith(".rar")) {
            return "comprimido";
        } else {
            return "archivo";
        }
    }
}

