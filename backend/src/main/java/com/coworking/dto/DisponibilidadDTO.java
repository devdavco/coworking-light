package com.coworking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadDTO {
    private Long espacioId;
    private LocalDate fecha;
    private List<Franja> franjas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Franja {
        private LocalTime horaInicio;
        private LocalTime horaFin;
    }
}
