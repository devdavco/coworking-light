package com.coworking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequest {

    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es requerido")
    private String email;

    @NotBlank(message = "Password es requerido")
    private String password;
}
