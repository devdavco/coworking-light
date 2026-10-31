package com.coworking.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {

    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es requerido")
    private String email;

    @Size(min = 8, max = 72, message = "Password debe tener entre 8 y 72 caracteres")
    @NotBlank(message = "Password es requerido")
    private String password;

    @NotBlank(message = "First name es requerido")
    @Size(max = 100, message = "First name admite como máximo 100 caracteres")
    private String firstName;

    @NotBlank(message = "Last name es requerido")
    @Size(max = 100, message = "Last name admite como máximo 100 caracteres")
    private String lastName;
}
