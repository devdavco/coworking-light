package com.coworking.controller;

import com.coworking.dto.ApiResponse;
import com.coworking.dto.DisponibilidadDTO;
import com.coworking.dto.EspacioDTO;
import com.coworking.dto.PaginatedResponse;
import com.coworking.service.EspacioService;
import com.coworking.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/espacios")
@Tag(name = "Espacios", description = "Gestión de espacios")
public class EspacioController {

    private final EspacioService espacioService;
    private final ReservaService reservaService;

    public EspacioController(EspacioService espacioService, ReservaService reservaService) {
        this.espacioService = espacioService;
        this.reservaService = reservaService;
    }

    @GetMapping
    @Operation(summary = "Listar espacios")
    public ResponseEntity<ApiResponse<PaginatedResponse<EspacioDTO>>> listar(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String tipo) {
        PaginatedResponse<EspacioDTO> response = espacioService.listarEspacios(page, size, tipo);
        return ResponseEntity.ok(new ApiResponse<>(true, "Espacios listados", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener espacio por ID")
    public ResponseEntity<ApiResponse<EspacioDTO>> obtenerPorId(@PathVariable Long id) {
        EspacioDTO response = espacioService.convertToDTO(espacioService.obtenerPorId(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Espacio encontrado", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear espacio (admin)")
    public ResponseEntity<ApiResponse<EspacioDTO>> crear(@Valid @RequestBody EspacioDTO dto) {
        EspacioDTO response = espacioService.convertToDTO(espacioService.crearEspacio(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Espacio creado", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar espacio (admin)")
    public ResponseEntity<ApiResponse<EspacioDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody EspacioDTO dto) {
        EspacioDTO response = espacioService.convertToDTO(espacioService.actualizarEspacio(id, dto));
        return ResponseEntity.ok(new ApiResponse<>(true, "Espacio actualizado", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar espacio (admin)")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        espacioService.eliminarEspacio(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Espacio eliminado", null));
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Obtener disponibilidad de un espacio")
    public ResponseEntity<ApiResponse<DisponibilidadDTO>> obtenerDisponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        DisponibilidadDTO response = reservaService.obtenerDisponibilidad(id, fecha);
        return ResponseEntity.ok(new ApiResponse<>(true, "Disponibilidad obtenida", response));
    }
}
