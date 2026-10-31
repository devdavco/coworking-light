package com.coworking.service;

import com.coworking.entity.Usuario;
import com.coworking.exception.DuplicateResourceException;
import com.coworking.exception.ResourceNotFoundException;
import com.coworking.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void testCrearUsuario() {
        when(usuarioRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        Usuario resultado = usuarioService.crearUsuario("test@test.com", "password123", "Juan", "Pérez", Usuario.UserRole.CLIENT);

        assertNotNull(resultado);
        assertEquals("test@test.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testCrearUsuarioDuplicado() {
        when(usuarioRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                usuarioService.crearUsuario("test@test.com", "password123", "Juan", "Pérez", Usuario.UserRole.CLIENT)
        );
    }

    @Test
    void testObtenerPorEmail() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("test@test.com")
                .firstName("Juan")
                .lastName("Pérez")
                .role(Usuario.UserRole.CLIENT)
                .build();

        when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.obtenerPorEmail("test@test.com");

        assertNotNull(resultado);
        assertEquals("test@test.com", resultado.getEmail());
    }

    @Test
    void testObtenerPorEmailNoEncontrado() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                usuarioService.obtenerPorEmail("noexiste@test.com")
        );
    }
}
