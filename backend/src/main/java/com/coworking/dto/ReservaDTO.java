package com.coworking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {
    private Long id;

    @NotNull(message = "Espacio ID es requerido")
    private Long espacioId;

    private Long usuarioId;

    @FutureOrPresent(message = "Fecha debe ser hoy o en el futuro")
    @NotNull(message = "Fecha es requerida")
    private LocalDate fecha;

    @NotNull(message = "Hora inicio es requerida")
    private LocalTime horaInicio;

    @NotNull(message = "Hora fin es requerida")
    private LocalTime horaFin;

    private String estado;

    private UsuarioDTO usuario;

    private EspacioDTO espacio;
}
