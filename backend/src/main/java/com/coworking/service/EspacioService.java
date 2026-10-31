package com.coworking.service;

import com.coworking.dto.EspacioDTO;
import com.coworking.dto.PaginatedResponse;
import com.coworking.entity.Espacio;
import com.coworking.exception.DuplicateResourceException;
import com.coworking.exception.ResourceNotFoundException;
import com.coworking.exception.ValidationException;
import com.coworking.repository.EspacioRepository;
import com.coworking.repository.ReservaRepository;
import com.coworking.util.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;

@Slf4j
@Service
@Transactional
public class EspacioService {

    private final EspacioRepository espacioRepository;
    private final ReservaRepository reservaRepository;

    public EspacioService(EspacioRepository espacioRepository, ReservaRepository reservaRepository) {
        this.espacioRepository = espacioRepository;
        this.reservaRepository = reservaRepository;
    }

    public Espacio crearEspacio(EspacioDTO dto) {
        if (espacioRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new DuplicateResourceException("Espacio con este nombre ya existe");
        }

        Espacio espacio = Espacio.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipo(parseTipo(dto.getTipo()))
                .capacidad(dto.getCapacidad())
                .horarioApertura(dto.getHorarioApertura())
                .horarioCierre(dto.getHorarioCierre())
                .build();

        validarHorario(espacio.getHorarioApertura(), espacio.getHorarioCierre());

        log.info("Creando espacio: {}", dto.getNombre());
        return espacioRepository.save(espacio);
    }

    @Transactional(readOnly = true)
    public Espacio obtenerPorId(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado"));
    }

    /** Igual que obtenerPorId pero bloqueando la fila (usar dentro de una transacción de escritura). */
    public Espacio obtenerPorIdParaActualizar(Long id) {
        return espacioRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado"));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<EspacioDTO> listarEspacios(Integer page, Integer size, String tipo) {
        Pageable pageable = PageUtils.of(page, size, Sort.by("nombre"));
        Page<Espacio> pageResult;

        if (tipo != null && !tipo.isBlank()) {
            pageResult = espacioRepository.findByTipo(parseTipo(tipo), pageable);
        } else {
            pageResult = espacioRepository.findAll(pageable);
        }

        return PaginatedResponse.of(pageResult, this::convertToDTO);
    }

    public Espacio actualizarEspacio(Long id, EspacioDTO dto) {
        Espacio espacio = obtenerPorId(id);

        if (dto.getNombre() != null && !dto.getNombre().equals(espacio.getNombre())) {
            if (espacioRepository.findByNombre(dto.getNombre()).isPresent()) {
                throw new DuplicateResourceException("Nombre ya existe");
            }
            espacio.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) {
            espacio.setDescripcion(dto.getDescripcion());
        }
        if (dto.getTipo() != null) {
            espacio.setTipo(parseTipo(dto.getTipo()));
        }
        if (dto.getCapacidad() != null) {
            espacio.setCapacidad(dto.getCapacidad());
        }
        if (dto.getHorarioApertura() != null) {
            espacio.setHorarioApertura(dto.getHorarioApertura());
        }
        if (dto.getHorarioCierre() != null) {
            espacio.setHorarioCierre(dto.getHorarioCierre());
        }
        validarHorario(espacio.getHorarioApertura(), espacio.getHorarioCierre());

        log.info("Actualizando espacio: {}", id);
        return espacioRepository.save(espacio);
    }

    public void eliminarEspacio(Long id) {
        obtenerPorId(id);

        // Las reservas (incluidas las canceladas) referencian al espacio por FK
        if (reservaRepository.existsByEspacioId(id)) {
            throw new ValidationException("No se puede eliminar un espacio con reservas asociadas");
        }

        log.info("Eliminando espacio: {}", id);
        espacioRepository.deleteById(id);
    }

    public EspacioDTO convertToDTO(Espacio espacio) {
        return EspacioDTO.builder()
                .id(espacio.getId())
                .nombre(espacio.getNombre())
                .descripcion(espacio.getDescripcion())
                .tipo(espacio.getTipo().name())
                .capacidad(espacio.getCapacidad())
                .horarioApertura(espacio.getHorarioApertura())
                .horarioCierre(espacio.getHorarioCierre())
                .build();
    }

    private Espacio.EspacioType parseTipo(String tipo) {
        try {
            return Espacio.EspacioType.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Tipo inválido. Valores permitidos: "
                    + Arrays.toString(Espacio.EspacioType.values()));
        }
    }

    private void validarHorario(LocalTime apertura, LocalTime cierre) {
        if (apertura != null && cierre != null && !apertura.isBefore(cierre)) {
            throw new ValidationException("El horario de apertura debe ser anterior al de cierre");
        }
    }
}
