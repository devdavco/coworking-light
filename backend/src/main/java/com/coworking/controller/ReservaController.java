package com.coworking.controller;

import com.coworking.dto.ApiResponse;
import com.coworking.dto.PaginatedResponse;
import com.coworking.dto.ReservaDTO;
import com.coworking.entity.Usuario;
import com.coworking.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Gestión de reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @Operation(summary = "Crear reserva")
    public ResponseEntity<ApiResponse<ReservaDTO>> crear(
            @Valid @RequestBody ReservaDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        ReservaDTO response = reservaService.crearReserva(email, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Reserva creada", response));
    }

    @GetMapping
    @Operation(summary = "Listar reservas (propias si es cliente, todas si es admin)")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReservaDTO>>> listar(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        String email = authentication.getName();
        Usuario.UserRole role = extraerRole(authentication);

        PaginatedResponse<ReservaDTO> response = reservaService.listarReservas(page, size, email, role);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservas listadas", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar reserva")
    public ResponseEntity<ApiResponse<Void>> cancelar(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        Usuario.UserRole role = extraerRole(authentication);

        reservaService.cancelarReserva(id, email, role);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva cancelada", null));
    }

    private Usuario.UserRole extraerRole(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                return Usuario.UserRole.ADMIN;
            }
        }
        return Usuario.UserRole.CLIENT;
    }
}
