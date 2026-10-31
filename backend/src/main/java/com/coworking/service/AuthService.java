package com.coworking.service;

import com.coworking.dto.AuthResponse;
import com.coworking.dto.AuthLoginRequest;
import com.coworking.dto.AuthRegisterRequest;
import com.coworking.entity.Usuario;
import com.coworking.exception.UnauthorizedException;
import com.coworking.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AuthService {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(AuthRegisterRequest request) {
        Usuario usuario = usuarioService.crearUsuario(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                Usuario.UserRole.CLIENT
        );

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        log.info("Usuario registrado: {}", usuario.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(usuarioService.convertToDTO(usuario))
                .build();
    }

    public AuthResponse login(AuthLoginRequest request) {
        // Mismo error para email inexistente y password incorrecto: no revelar qué emails existen
        Usuario usuario = usuarioService.buscarPorEmail(request.getEmail())
                .filter(u -> usuarioService.validarPassword(request.getPassword(), u.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido para: {}", request.getEmail());
                    return new UnauthorizedException("Credenciales inválidas");
                });

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        log.info("Usuario logueado: {}", usuario.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(usuarioService.convertToDTO(usuario))
                .build();
    }
}
