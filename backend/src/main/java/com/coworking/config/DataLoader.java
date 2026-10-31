package com.coworking.config;

import com.coworking.entity.Espacio;
import com.coworking.entity.Usuario;
import com.coworking.repository.EspacioRepository;
import com.coworking.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Configuration
public class DataLoader {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public ApplicationRunner loadData(UsuarioRepository usuarioRepository,
                                      EspacioRepository espacioRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            crearAdminSiNoExiste(usuarioRepository, passwordEncoder);
            crearEspaciosDePrueba(espacioRepository);
        };
    }

    private void crearAdminSiNoExiste(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        if (usuarioRepository.existsByEmail(adminEmail)) {
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD no definido: no se crea el usuario administrador");
            return;
        }

        Usuario admin = Usuario.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName("Admin")
                .lastName("Sistema")
                .role(Usuario.UserRole.ADMIN)
                .build();
        usuarioRepository.save(admin);
        log.info("Admin creado: {}", adminEmail);
    }

    private void crearEspaciosDePrueba(EspacioRepository espacioRepository) {
        if (espacioRepository.count() > 0) {
            return;
        }

        espacioRepository.saveAll(List.of(
                espacio("Oficina Premium", "Oficina privada para 4 personas", Espacio.EspacioType.OFICINA, 4),
                espacio("Sala de Datos", "Sala de reuniones equipada con proyector", Espacio.EspacioType.SALA_REUNIONES, 10),
                espacio("Espacio Coworking Abierto", "Área abierta con escritorios compartidos", Espacio.EspacioType.COWORKING_ABIERTO, 20)
        ));
        log.info("Espacios de prueba creados");
    }

    private Espacio espacio(String nombre, String descripcion, Espacio.EspacioType tipo, int capacidad) {
        return Espacio.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .tipo(tipo)
                .capacidad(capacidad)
                .horarioApertura(LocalTime.of(7, 0))
                .horarioCierre(LocalTime.of(22, 0))
                .build();
    }
}
