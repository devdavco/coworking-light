package com.coworking.repository;

import com.coworking.entity.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @EntityGraph(attributePaths = {"usuario", "espacio"})
    Page<Reserva> findByUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "espacio"})
    Page<Reserva> findAll(Pageable pageable);

    @Query("""
            SELECT r FROM Reserva r 
            WHERE r.espacio.id = :espacioId 
            AND r.fecha = :fecha 
            AND r.estado = 'CONFIRMADA'
            AND (
                (r.horaInicio < :horaFin AND r.horaFin > :horaInicio)
            )
            """)
    List<Reserva> findConflictingReservas(
            @Param("espacioId") Long espacioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    @Query("""
            SELECT r FROM Reserva r 
            WHERE r.espacio.id = :espacioId 
            AND r.fecha = :fecha 
            AND r.estado = 'CONFIRMADA'
            ORDER BY r.horaInicio
            """)
    List<Reserva> findReservasByEspacioAndFecha(
            @Param("espacioId") Long espacioId,
            @Param("fecha") LocalDate fecha
    );

    boolean existsByEspacioId(Long espacioId);
}
