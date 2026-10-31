package com.coworking.service;

import com.coworking.dto.DisponibilidadDTO;
import com.coworking.entity.Espacio;
import com.coworking.entity.Reserva;
import com.coworking.entity.Usuario;
import com.coworking.exception.ValidationException;
import com.coworking.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    // "Ahora" fijo: 2030-01-15 10:00 hora local
    private static final Clock CLOCK = Clock.fixed(
            LocalDate.of(2030, 1, 15).atTime(10, 0).atZone(ZONA).toInstant(), ZONA);
    private static final LocalDate MANANA = LocalDate.of(2030, 1, 16);

    private ReservaRepository reservaRepository;
    private UsuarioService usuarioService;
    private ReservaService reservaService;
    private Espacio espacio;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        usuarioService = mock(UsuarioService.class);
        EspacioService espacioService = mock(EspacioService.class);
        reservaService = new ReservaService(reservaRepository, usuarioService, espacioService, CLOCK);

        espacio = Espacio.builder()
                .id(1L)
                .nombre("Sala")
                .tipo(Espacio.EspacioType.SALA_REUNIONES)
                .capacidad(10)
                .horarioApertura(LocalTime.of(7, 0))
                .horarioCierre(LocalTime.of(22, 0))
                .build();
        when(reservaRepository.findConflictingReservas(any(), any(), any(), any())).thenReturn(List.of());
    }

    @Test
    void reservaValidaNoLanzaExcepcion() {
        assertDoesNotThrow(() -> reservaService.validarReserva(espacio, MANANA, LocalTime.of(9, 0), LocalTime.of(11, 0)));
    }

    @Test
    void rechazaDuracionMenorDeUnaHora() {
        assertThrows(ValidationException.class,
                () -> reservaService.validarReserva(espacio, MANANA, LocalTime.of(9, 0), LocalTime.of(9, 30)));
    }

    @Test
    void rechazaDuracionMayorDeOchoHorasAunqueSeaPorMinutos() {
        assertThrows(ValidationException.class,
                () -> reservaService.validarReserva(espacio, MANANA, LocalTime.of(8, 0), LocalTime.of(16, 30)));
    }

    @Test
    void rechazaHoraFinAnteriorAInicio() {
        assertThrows(ValidationException.class,
                () -> reservaService.validarReserva(espacio, MANANA, LocalTime.of(12, 0), LocalTime.of(10, 0)));
    }

    @Test
    void rechazaFueraDeHorarioDelEspacio() {
        assertThrows(ValidationException.class,
                () -> reservaService.validarReserva(espacio, MANANA, LocalTime.of(6, 0), LocalTime.of(8, 0)));
        assertThrows(ValidationException.class,
                () -> reservaService.validarReserva(espacio, MANANA, LocalTime.of(21, 0), LocalTime.of(23, 0)));
    }

    @Test
    void rechazaHoraYaPasadaDeHoy() {
        LocalDate hoy = LocalDate.now(CLOCK);
        assertThrows(ValidationException.class,
                () -> reservaService.validarReserva(espacio, hoy, LocalTime.of(8, 0), LocalTime.of(9, 0)));
    }

    @Test
    void rechazaSolapamiento() {
        when(reservaRepository.findConflictingReservas(any(), any(), any(), any()))
                .thenReturn(List.of(reserva(LocalTime.of(10, 0), LocalTime.of(12, 0))));

        assertThrows(ValidationException.class,
                () -> reservaService.validarReserva(espacio, MANANA, LocalTime.of(11, 0), LocalTime.of(13, 0)));
    }

    @Test
    void calculaFranjasLibresEntreReservas() {
        List<DisponibilidadDTO.Franja> franjas = reservaService.calcularFranjasLibres(espacio, List.of(
                reserva(LocalTime.of(9, 0), LocalTime.of(11, 0)),
                reserva(LocalTime.of(14, 0), LocalTime.of(16, 0))
        ));

        assertEquals(3, franjas.size());
        assertEquals(LocalTime.of(7, 0), franjas.get(0).getHoraInicio());
        assertEquals(LocalTime.of(9, 0), franjas.get(0).getHoraFin());
        assertEquals(LocalTime.of(11, 0), franjas.get(1).getHoraInicio());
        assertEquals(LocalTime.of(14, 0), franjas.get(1).getHoraFin());
        assertEquals(LocalTime.of(16, 0), franjas.get(2).getHoraInicio());
        assertEquals(LocalTime.of(22, 0), franjas.get(2).getHoraFin());
    }

    @Test
    void clienteNoPuedeCancelarReservaAjena() {
        Usuario propietario = Usuario.builder().id(1L).email("a@test.com").role(Usuario.UserRole.CLIENT).build();
        Usuario otro = Usuario.builder().id(2L).email("b@test.com").role(Usuario.UserRole.CLIENT).build();
        Reserva r = reserva(LocalTime.of(9, 0), LocalTime.of(10, 0));
        r.setUsuario(propietario);

        when(reservaRepository.findById(5L)).thenReturn(Optional.of(r));
        when(usuarioService.obtenerPorEmail("b@test.com")).thenReturn(otro);

        assertThrows(AccessDeniedException.class,
                () -> reservaService.cancelarReserva(5L, "b@test.com", Usuario.UserRole.CLIENT));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void adminPuedeCancelarCualquierReserva() {
        Reserva r = reserva(LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(r));

        reservaService.cancelarReserva(5L, "admin@test.com", Usuario.UserRole.ADMIN);

        assertEquals(Reserva.ReservaStatus.CANCELADA, r.getEstado());
        verify(reservaRepository).save(r);
    }

    private Reserva reserva(LocalTime inicio, LocalTime fin) {
        return Reserva.builder()
                .espacio(espacio)
                .fecha(MANANA)
                .horaInicio(inicio)
                .horaFin(fin)
                .estado(Reserva.ReservaStatus.CONFIRMADA)
                .build();
    }
}
