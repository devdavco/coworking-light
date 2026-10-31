package com.coworking.service;

import com.coworking.dto.UsuarioDTO;
import com.coworking.entity.Usuario;
import com.coworking.exception.DuplicateResourceException;
import com.coworking.exception.ResourceNotFoundException;
import com.coworking.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario crearUsuario(String email, String password, String firstName, String lastName, Usuario.UserRole role) {
        String emailNormalizado = normalizarEmail(email);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new DuplicateResourceException("Email ya existe");
        }

        Usuario usuario = Usuario.builder()
                .email(emailNormalizado)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .role(role)
                .build();

        log.info("Creando usuario: {}", emailNormalizado);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email));
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return buscarPorEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public UsuarioDTO convertToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .firstName(usuario.getFirstName())
                .lastName(usuario.getLastName())
                .role(usuario.getRole().name())
                .build();
    }

    public boolean validarPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
