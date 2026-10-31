package com.coworking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "espacios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EspacioType tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private LocalTime horarioApertura;

    @Column(nullable = false)
    private LocalTime horarioCierre;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (horarioApertura == null) {
            horarioApertura = LocalTime.of(7, 0);
        }
        if (horarioCierre == null) {
            horarioCierre = LocalTime.of(22, 0);
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EspacioType {
        OFICINA, SALA_REUNIONES, COWORKING_ABIERTO
    }
}
