package com.coworking.util;

import com.coworking.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static JwtProperties props(String secret, long expiration) {
        JwtProperties p = new JwtProperties();
        p.setSecret(secret);
        p.setExpiration(expiration);
        p.setRefreshExpiration(expiration);
        return p;
    }

    private final JwtUtil jwtUtil = new JwtUtil(props("test-secret-key-with-at-least-32-bytes-long!!", 60_000));

    @Test
    void generaYValidaToken() {
        String token = jwtUtil.generateToken("user@test.com", "CLIENT");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("user@test.com", jwtUtil.getEmailFromToken(token));
        assertEquals("CLIENT", jwtUtil.getRoleFromToken(token));
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        JwtUtil otro = new JwtUtil(props("another-secret-key-with-at-least-32-bytes!!!", 60_000));
        String token = otro.generateToken("user@test.com", "ADMIN");

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void rechazaTokenExpirado() {
        JwtUtil caducado = new JwtUtil(props("test-secret-key-with-at-least-32-bytes-long!!", -1_000));
        String token = caducado.generateToken("user@test.com", "CLIENT");

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void rechazaSecretoDemasiadoCorto() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil(props("corto", 60_000)));
    }
}
