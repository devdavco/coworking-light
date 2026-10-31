package com.coworking.repository;

import com.coworking.entity.Espacio;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Long> {
    Optional<Espacio> findByNombre(String nombre);
    Page<Espacio> findByTipo(Espacio.EspacioType tipo, Pageable pageable);

    /**
     * Bloquea la fila del espacio durante la transacción para serializar
     * la creación de reservas concurrentes sobre el mismo espacio.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Espacio e WHERE e.id = :id")
    Optional<Espacio> findByIdForUpdate(@Param("id") Long id);
}
