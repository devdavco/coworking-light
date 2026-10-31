package com.coworking.service;

import com.coworking.dto.AuthLoginRequest;
import com.coworking.dto.AuthRegisterRequest;
import com.coworking.dto.AuthResponse;
import com.coworking.entity.Usuario;
import com.coworking.exception.UnauthorizedException;
import com.coworking.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        AuthRegisterRequest request = new AuthRegisterRequest("test@test.com", "password123", "Juan", "Pérez");
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("test@test.com")
                .firstName("Juan")
                .lastName("Pérez")
                .role(Usuario.UserRole.CLIENT)
                .build();

        when(usuarioService.crearUsuario(anyString(), anyString(), anyString(), anyString(), any(Usuario.UserRole.class)))
                .thenReturn(usuario);
        when(jwtUtil.generateToken("test@test.com", "CLIENT")).thenReturn("token");
        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("refreshToken");
        when(usuarioService.convertToDTO(usuario)).thenCallRealMethod();

        AuthResponse resultado = authService.register(request);

        assertNotNull(resultado);
        assertNotNull(resultado.getToken());
    }

    @Test
    void testLoginExito() {
        AuthLoginRequest request = new AuthLoginRequest("test@test.com", "password123");
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("test@test.com")
                .passwordHash("hashedPassword")
                .firstName("Juan")
                .lastName("Pérez")
                .role(Usuario.UserRole.CLIENT)
                .build();

        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioService.validarPassword("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", "CLIENT")).thenReturn("token");
        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("refreshToken");
        when(usuarioService.convertToDTO(usuario)).thenCallRealMethod();

        AuthResponse resultado = authService.login(request);

        assertNotNull(resultado);
        assertNotNull(resultado.getToken());
    }

    @Test
    void testLoginCredencialesInvalidas() {
        AuthLoginRequest request = new AuthLoginRequest("test@test.com", "passwordIncorrecto");
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("test@test.com")
                .passwordHash("hashedPassword")
                .firstName("Juan")
                .lastName("Pérez")
                .role(Usuario.UserRole.CLIENT)
                .build();

        when(usuarioService.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioService.validarPassword("passwordIncorrecto", "hashedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLoginEmailInexistenteDevuelveUnauthorized() {
        when(usuarioService.buscarPorEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new AuthLoginRequest("noexiste@test.com", "password123")));
    }
}
