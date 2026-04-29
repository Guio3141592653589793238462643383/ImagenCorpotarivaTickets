package com.austral.back.services.ticket;

import com.austral.back.model.Comentario;
import com.austral.back.model.Media;
import com.austral.back.model.Ticket;
import com.austral.back.model.TicketMedia;
import com.austral.back.model.SolicitudMaterial;
import com.austral.back.model.SolicitudMaterialItem;
import com.austral.back.model.SolicitudProductoItem;
import com.austral.back.model.Rol;
import com.austral.back.dto.ComentarioDTO;
import com.austral.back.repository.ComentarioRepository;
import com.austral.back.repository.TicketRepository;
import com.austral.back.repository.UsuarioRepository;
import com.austral.back.services.notificacion.NotificacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ComentarioRepository comentarioRepository;
    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public TicketService(TicketRepository ticketRepository,
                         ComentarioRepository comentarioRepository,
                         NotificacionService notificacionService,
                         UsuarioRepository usuarioRepository,
                         SimpMessagingTemplate messagingTemplate) {
        this.ticketRepository = ticketRepository;
        this.comentarioRepository = comentarioRepository;
        this.notificacionService = notificacionService;
        this.usuarioRepository = usuarioRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Crea un nuevo ticket con archivos adjuntos y solicitud de material
     */
    public Ticket crearTicket(String descripcion,
                              String tema,
                              String tipoDePregunta,
                              String nombreCompleto,
                              String correoElectronico,
                              String numeroTelefono,
                              MultipartFile[] archivos,
                              // ── Solicitud de material ──
                              String decoracion,
                              String producto,
                              Integer cantidad,
                              Double largo,
                              Double ancho,
                              List<String> productos,
                              List<String> cantidadesProductos,
                              boolean ayudaventasImpresos,
                              boolean listasDePrecios,
                              boolean muestrasLentes,
                              boolean regaloCorporativo,
                              boolean materialCapacitaciones,
                              boolean opcion6,
                              boolean paniosMarcados,
                              boolean libretaNotas,
                              boolean reglillas,
                              boolean videosUsb,
                              boolean esferos,
                              boolean habladores) throws Exception {

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Ticket ticket = new Ticket();
        ticket.setMarcaTemporal(LocalDateTime.now());
        ticket.setNombreCompleto(nombreCompleto);
        ticket.setTipoDePregunta(tipoDePregunta);
        ticket.setTema(tema);
        ticket.setDescription(descripcion);
        ticket.setCorreoElectronico(correoElectronico);
        ticket.setNumeroTelefono(numeroTelefono);
        ticket.setEstado("ABIERTO");
        ticket.setPriority("BAJA");

        // Vincular con el usuario registrado (si existe)
        usuarioRepository.findByEmail(correoElectronico)
                .ifPresent(ticket::setUsuario);

        // ── Archivos adjuntos ──
        List<TicketMedia> mediaList = new ArrayList<>();
        if (archivos != null && archivos.length > 0) {
            for (MultipartFile file : archivos) {
                if (!file.isEmpty()) {
                    String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path rutaArchivo = Paths.get(uploadDir + nombreArchivo);
                    Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

                    TicketMedia media = new TicketMedia();
                    media.setMediaPath(nombreArchivo);
                    media.setTicket(ticket);
                    mediaList.add(media);
                }
            }
        }
        ticket.setMediaList(mediaList);

        // ── Solicitud de material ──
        SolicitudMaterial solicitud = new SolicitudMaterial();
        solicitud.setTicket(ticket);
        solicitud.setDecoracion(decoracion);
        solicitud.setProducto(producto);
        solicitud.setCantidad(cantidad);
        solicitud.setLargo(largo);
        solicitud.setAncho(ancho);
        solicitud.setAyudaventasImpresos(ayudaventasImpresos);
        solicitud.setListasDePrecios(listasDePrecios);
        solicitud.setMuestrasLentes(muestrasLentes);
        solicitud.setRegaloCorporativo(regaloCorporativo);
        solicitud.setMaterialCapacitaciones(materialCapacitaciones);
        solicitud.setOpcion6(opcion6);
        solicitud.setPaniosMarcados(paniosMarcados);
        solicitud.setLibretaNotas(libretaNotas);
        solicitud.setReglillas(reglillas);
        solicitud.setVideosUsb(videosUsb);
        solicitud.setEsferos(esferos);
        solicitud.setHabladores(habladores);
        solicitud.setProductos(crearProductosSolicitud(solicitud, producto, cantidad, productos, cantidadesProductos));
        solicitud.setMateriales(crearMaterialesSolicitud(
                solicitud,
                ayudaventasImpresos,
                listasDePrecios,
                muestrasLentes,
                regaloCorporativo,
                materialCapacitaciones,
                paniosMarcados,
                libretaNotas,
                reglillas,
                videosUsb,
                esferos,
                habladores
        ));

        ticket.setSolicitudMaterial(solicitud);

        Ticket ticketGuardado = ticketRepository.save(ticket);

        // 🔔 Notificación SOLO para ADMIN
        notificacionService.crear(
                "Nuevo ticket creado por " + nombreCompleto + " (" + correoElectronico + ")",
                "NUEVO_TICKET",
                ticketGuardado,
                Rol.ADMIN
        );

        return ticketGuardado;
    }

    private List<SolicitudProductoItem> crearProductosSolicitud(SolicitudMaterial solicitud,
                                                                String producto,
                                                                Integer cantidad,
                                                                List<String> productos,
                                                                List<String> cantidadesProductos) {
        List<SolicitudProductoItem> items = new ArrayList<>();

        if (producto != null && !producto.isBlank()) {
            agregarProducto(items, solicitud, producto, cantidad);
        }

        if (productos != null) {
            for (int i = 0; i < productos.size(); i++) {
                String productoActual = productos.get(i);
                Integer cantidadActual = cantidadesProductos != null && i < cantidadesProductos.size()
                        ? parseCantidad(cantidadesProductos.get(i))
                        : null;
                agregarProducto(items, solicitud, productoActual, cantidadActual);
            }
        }

        return items;
    }

    private Integer parseCantidad(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void agregarProducto(List<SolicitudProductoItem> items,
                                 SolicitudMaterial solicitud,
                                 String producto,
                                 Integer cantidad) {
        if (producto == null || producto.isBlank()) {
            return;
        }

        SolicitudProductoItem item = new SolicitudProductoItem();
        item.setSolicitud(solicitud);
        item.setProductoId(producto.trim());
        item.setCantidad(cantidad != null ? cantidad : 1);
        items.add(item);
    }

    private List<SolicitudMaterialItem> crearMaterialesSolicitud(SolicitudMaterial solicitud,
                                                                 boolean ayudaventasImpresos,
                                                                 boolean listasDePrecios,
                                                                 boolean muestrasLentes,
                                                                 boolean regaloCorporativo,
                                                                 boolean materialCapacitaciones,
                                                                 boolean paniosMarcados,
                                                                 boolean libretaNotas,
                                                                 boolean reglillas,
                                                                 boolean videosUsb,
                                                                 boolean esferos,
                                                                 boolean habladores) {
        List<SolicitudMaterialItem> materiales = new ArrayList<>();
        agregarMaterial(materiales, solicitud, ayudaventasImpresos, "Ayudaventas Impresos");
        agregarMaterial(materiales, solicitud, listasDePrecios, "Listas de Precios");
        agregarMaterial(materiales, solicitud, muestrasLentes, "Muestras de Lentes");
        agregarMaterial(materiales, solicitud, regaloCorporativo, "Regalos Corporativos");
        agregarMaterial(materiales, solicitud, materialCapacitaciones, "Material Capacitaciones");
        agregarMaterial(materiales, solicitud, paniosMarcados, "Panos Marcados");
        agregarMaterial(materiales, solicitud, libretaNotas, "Libreta de Notas");
        agregarMaterial(materiales, solicitud, reglillas, "Reglillas");
        agregarMaterial(materiales, solicitud, videosUsb, "Videos - USB");
        agregarMaterial(materiales, solicitud, esferos, "Esferos");
        agregarMaterial(materiales, solicitud, habladores, "Habladores");
        return materiales;
    }

    private void agregarMaterial(List<SolicitudMaterialItem> materiales,
                                 SolicitudMaterial solicitud,
                                 boolean seleccionado,
                                 String materialId) {
        if (!seleccionado) {
            return;
        }

        SolicitudMaterialItem item = new SolicitudMaterialItem();
        item.setSolicitud(solicitud);
        item.setMaterialId(materialId);
        item.setCantidad(1);
        materiales.add(item);
    }

    public Page<Ticket> obtenerTodosOrdenados(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    public List<Ticket> obtenerTicketsUsuario(String email) {
        return ticketRepository.findByCorreoElectronicoOrderByMarcaTemporalDesc(email);
    }

    public Page<Ticket> obtenerTicketsUsuario(String email, Pageable pageable) {
        return ticketRepository.findByCorreoElectronicoOrderByMarcaTemporalDesc(email, pageable);
    }

    public List<Ticket> obtenerTodosOrdenados() {
        return ticketRepository.findAllByOrderByMarcaTemporalDesc();
    }

    public Ticket obtenerPorId(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    public Ticket guardar(Ticket ticket) {
        if ("CERRADO".equalsIgnoreCase(ticket.getEstado()) && ticket.getFechaCierre() == null) {
            ticket.setFechaCierre(LocalDateTime.now());
        }
        if (!"CERRADO".equalsIgnoreCase(ticket.getEstado())) {
            ticket.setFechaCierre(null);
        }
        return ticketRepository.save(ticket);
    }

    public List<Ticket> filtrarTickets(String estado, String prioridad, String busqueda) {
        return ticketRepository.findAllByOrderByMarcaTemporalDesc()
                .stream()
                .filter(t -> estado == null || estado.isEmpty() || t.getEstado().equalsIgnoreCase(estado))
                .filter(t -> prioridad == null || prioridad.isEmpty() || t.getPriority().equalsIgnoreCase(prioridad))
                .filter(t -> busqueda == null || busqueda.isEmpty()
                        || t.getCorreoElectronico().toLowerCase().contains(busqueda.toLowerCase())
                        || t.getId().toString().contains(busqueda))
                .toList();
    }

    public Page<Ticket> filtrarTickets(String estado, String prioridad, String busqueda, Pageable pageable) {
        Specification<Ticket> spec = (root, query, cb) -> cb.conjunction();

        if (estado != null && !estado.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("estado")), estado.trim().toLowerCase()));
        }

        if (prioridad != null && !prioridad.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("priority")), prioridad.trim().toLowerCase()));
        }

        if (busqueda != null && !busqueda.isBlank()) {
            String like = "%" + busqueda.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("correoElectronico")), like),
                    cb.like(cb.lower(root.get("nombreCompleto")), like),
                    cb.like(cb.lower(root.get("tema")), like),
                    cb.like(root.get("id").as(String.class), like)
            ));
        }

        return ticketRepository.findAll(spec, pageable);
    }

    /**
     * Agrega respuesta del usuario
     */
    public void agregarRespuestaUsuario(Integer ticketId, String mensaje, MultipartFile[] archivos) throws Exception {
        Ticket ticket = obtenerPorId(ticketId);

        Comentario comentario = new Comentario();
        comentario.setMensaje(mensaje);
        comentario.setRemitente(ticket.getNombreActual());
        comentario.setMarcaTemporal(LocalDateTime.now());
        comentario.setTicket(ticket);

        List<Media> mediaList = new ArrayList<>();
        if (archivos != null && archivos.length > 0) {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            for (MultipartFile file : archivos) {
                if (!file.isEmpty()) {
                    String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path rutaArchivo = Paths.get(uploadDir + nombreArchivo);
                    Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

                    Media media = new Media();
                    media.setMediaPath(nombreArchivo);
                    media.setComentario(comentario);
                    mediaList.add(media);
                }
            }
            comentario.setTieneImagenes(!mediaList.isEmpty());
        } else {
            comentario.setTieneImagenes(false);
        }

        comentario.setMedia(mediaList);
        comentarioRepository.save(comentario);

        // Enviar comentario en tiempo real por WebSocket
        messagingTemplate.convertAndSend(
                "/topic/ticket/" + ticketId, new ComentarioDTO(comentario));

        // 🔔 Notificación SOLO para ADMIN
        notificacionService.crear(
                "Nuevo comentario del usuario en el ticket #" + ticket.getId(),
                "COMENTARIO",
                ticket,
                Rol.ADMIN
        );
    }

    public List<Comentario> obtenerComentariosPorTicket(Integer ticketId) {
        return comentarioRepository.findByTicketIdOrderByMarcaTemporalAsc(ticketId);
    }

    /**
     * Agrega respuesta del admin
     */
    public void agregarRespuestaAdmin(Integer ticketId, String mensaje, MultipartFile[] archivos, String nombreAdmin) throws Exception {
        Ticket ticket = obtenerPorId(ticketId);

        Comentario comentario = new Comentario();
        comentario.setMensaje(mensaje);
        comentario.setRemitente(nombreAdmin != null ? nombreAdmin : "ADMIN");
        comentario.setMarcaTemporal(LocalDateTime.now());
        comentario.setTicket(ticket);

        List<Media> mediaList = new ArrayList<>();
        if (archivos != null && archivos.length > 0) {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            for (MultipartFile file : archivos) {
                if (!file.isEmpty()) {
                    String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path rutaArchivo = Paths.get(uploadDir + nombreArchivo);
                    Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

                    Media media = new Media();
                    media.setMediaPath(nombreArchivo);
                    media.setComentario(comentario);
                    mediaList.add(media);
                }
            }
            comentario.setTieneImagenes(!mediaList.isEmpty());
        }

        comentario.setMedia(mediaList);
        comentarioRepository.save(comentario);

        // Enviar comentario en tiempo real por WebSocket
        messagingTemplate.convertAndSend(
                "/topic/ticket/" + ticketId, new ComentarioDTO(comentario));

        // 🔔 Notificación al dueño del ticket
        if (ticket.getUsuario() != null) {
            notificacionService.crearParaUsuario(
                    "Respuesta del administrador en tu ticket #" + ticket.getId(),
                    "RESPUESTA_ADMIN",
                    ticket,
                    ticket.getUsuario()
            );
        }
    }
}
