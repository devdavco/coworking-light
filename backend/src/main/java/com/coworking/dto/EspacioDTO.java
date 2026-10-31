package com.coworking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioDTO {
    private Long id;

    @NotBlank(message = "Nombre es requerido")
    @Size(max = 255, message = "Nombre admite como máximo 255 caracteres")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "Tipo es requerido")
    private String tipo;

    @NotNull(message = "Capacidad es requerida")
    @Positive(message = "Capacidad debe ser mayor a 0")
    private Integer capacidad;

    private LocalTime horarioApertura;

    private LocalTime horarioCierre;
}
