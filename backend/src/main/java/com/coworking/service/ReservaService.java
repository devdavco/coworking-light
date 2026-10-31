package com.coworking.service;

import com.coworking.dto.DisponibilidadDTO;
import com.coworking.dto.PaginatedResponse;
import com.coworking.dto.ReservaDTO;
import com.coworking.entity.Espacio;
import com.coworking.entity.Reserva;
import com.coworking.entity.Usuario;
import com.coworking.exception.ResourceNotFoundException;
import com.coworking.exception.ValidationException;
import com.coworking.repository.ReservaRepository;
import com.coworking.util.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ReservaService {

    static final Duration DURACION_MINIMA = Duration.ofHours(1);
    static final Duration DURACION_MAXIMA = Duration.ofHours(8);

    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;
    private final EspacioService espacioService;
    private final Clock clock;

    public ReservaService(ReservaRepository reservaRepository, UsuarioService usuarioService,
                          EspacioService espacioService, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
        this.espacioService = espacioService;
        this.clock = clock;
    }

    public ReservaDTO crearReserva(String emailUsuario, ReservaDTO dto) {
        Usuario usuario = usuarioService.obtenerPorEmail(emailUsuario);
        // Bloqueo de la fila del espacio: dos peticiones simultáneas no pueden
        // pasar ambas la comprobación de solapamiento
        Espacio espacio = espacioService.obtenerPorIdParaActualizar(dto.getEspacioId());

        validarReserva(espacio, dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin());

        Reserva reserva = Reserva.builder()
                .usuario(usuario)
                .espacio(espacio)
                .fecha(dto.getFecha())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .estado(Reserva.ReservaStatus.CONFIRMADA)
                .build();

        log.info("Creando reserva para usuario: {} en espacio: {}", emailUsuario, espacio.getId());
        return convertToDTO(reservaRepository.save(reserva));
    }

    public void validarReserva(Espacio espacio, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (!horaInicio.isBefore(horaFin)) {
            throw new ValidationException("La hora de inicio debe ser anterior a la hora de fin");
        }

        if (LocalDateTime.of(fecha, horaInicio).isBefore(LocalDateTime.now(clock))) {
            throw new ValidationException("No se puede reservar en el pasado");
        }

        if (horaInicio.isBefore(espacio.getHorarioApertura()) || horaFin.isAfter(espacio.getHorarioCierre())) {
            throw new ValidationException("Horario debe estar entre " + espacio.getHorarioApertura() + " y " + espacio.getHorarioCierre());
        }

        Duration duracion = Duration.between(horaInicio, horaFin);
        if (duracion.compareTo(DURACION_MINIMA) < 0 || duracion.compareTo(DURACION_MAXIMA) > 0) {
            throw new ValidationException("Duración debe estar entre " + DURACION_MINIMA.toHours() + " y " + DURACION_MAXIMA.toHours() + " horas");
        }

        List<Reserva> conflictivas = reservaRepository.findConflictingReservas(espacio.getId(), fecha, horaInicio, horaFin);
        if (!conflictivas.isEmpty()) {
            throw new ValidationException("Horario no disponible");
        }
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ReservaDTO> listarReservas(Integer page, Integer size, String emailUsuario, Usuario.UserRole role) {
        Pageable pageable = PageUtils.of(page, size, Sort.by(Sort.Direction.DESC, "fecha", "horaInicio"));
        Page<Reserva> pageResult;

        if (role == Usuario.UserRole.CLIENT) {
            Usuario usuario = usuarioService.obtenerPorEmail(emailUsuario);
            pageResult = reservaRepository.findByUsuarioId(usuario.getId(), pageable);
        } else {
            pageResult = reservaRepository.findAll(pageable);
        }

        return PaginatedResponse.of(pageResult, this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    public void cancelarReserva(Long id, String emailUsuario, Usuario.UserRole role) {
        Reserva reserva = obtenerPorId(id);

        if (role == Usuario.UserRole.CLIENT) {
            Usuario usuario = usuarioService.obtenerPorEmail(emailUsuario);
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                throw new AccessDeniedException("Solo puedes cancelar tus propias reservas");
            }
        }

        if (reserva.getEstado() == Reserva.ReservaStatus.CANCELADA) {
            throw new ValidationException("La reserva ya está cancelada");
        }

        reserva.setEstado(Reserva.ReservaStatus.CANCELADA);
        log.info("Reserva cancelada: {}", id);
        reservaRepository.save(reserva);
    }

    @Transactional(readOnly = true)
    public DisponibilidadDTO obtenerDisponibilidad(Long espacioId, LocalDate fecha) {
        Espacio espacio = espacioService.obtenerPorId(espacioId);

        List<Reserva> reservasDelDia = reservaRepository.findReservasByEspacioAndFecha(espacioId, fecha);

        return DisponibilidadDTO.builder()
                .espacioId(espacioId)
                .fecha(fecha)
                .franjas(calcularFranjasLibres(espacio, reservasDelDia))
                .build();
    }

    /** Requiere que las reservas vengan ordenadas por hora de inicio. */
    List<DisponibilidadDTO.Franja> calcularFranjasLibres(Espacio espacio, List<Reserva> reservas) {
        List<DisponibilidadDTO.Franja> franjas = new ArrayList<>();
        LocalTime actual = espacio.getHorarioApertura();

        for (Reserva reserva : reservas) {
            if (actual.isBefore(reserva.getHoraInicio())) {
                franjas.add(new DisponibilidadDTO.Franja(actual, reserva.getHoraInicio()));
            }
            if (reserva.getHoraFin().isAfter(actual)) {
                actual = reserva.getHoraFin();
            }
        }

        if (actual.isBefore(espacio.getHorarioCierre())) {
            franjas.add(new DisponibilidadDTO.Franja(actual, espacio.getHorarioCierre()));
        }

        return franjas;
    }

    public ReservaDTO convertToDTO(Reserva reserva) {
        return ReservaDTO.builder()
                .id(reserva.getId())
                .espacioId(reserva.getEspacio().getId())
                .usuarioId(reserva.getUsuario().getId())
                .fecha(reserva.getFecha())
                .horaInicio(reserva.getHoraInicio())
                .horaFin(reserva.getHoraFin())
                .estado(reserva.getEstado().name())
                .usuario(usuarioService.convertToDTO(reserva.getUsuario()))
                .espacio(espacioService.convertToDTO(reserva.getEspacio()))
                .build();
    }
}
