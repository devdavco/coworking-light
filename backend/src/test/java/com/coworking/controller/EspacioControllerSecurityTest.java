package com.coworking.controller;

import com.coworking.config.CorsConfig;
import com.coworking.config.JwtProperties;
import com.coworking.config.SecurityConfig;
import com.coworking.service.EspacioService;
import com.coworking.service.ReservaService;
import com.coworking.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la cadena de seguridad real (filtro JWT + reglas + @PreAuthorize).
 */
@WebMvcTest(EspacioController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtUtil.class, JwtProperties.class})
class EspacioControllerSecurityTest {

    private static final String BODY = """
            {"nombre":"Sala Nueva","tipo":"OFICINA","capacidad":4}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private EspacioService espacioService;

    @MockBean
    private ReservaService reservaService;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/espacios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/espacios").header("Authorization", "Bearer no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeCrearEspacios() throws Exception {
        String token = jwtUtil.generateToken("cliente@test.com", "CLIENT");

        mockMvc.perform(post("/api/espacios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(espacioService, never()).crearEspacio(any());
    }

    @Test
    void refreshTokenNoDaAccesoALaApi() throws Exception {
        String refresh = jwtUtil.generateRefreshToken("cliente@test.com");

        mockMvc.perform(get("/api/espacios").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteAutenticadoPuedeListarEspacios() throws Exception {
        String token = jwtUtil.generateToken("cliente@test.com", "CLIENT");

        mockMvc.perform(get("/api/espacios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
