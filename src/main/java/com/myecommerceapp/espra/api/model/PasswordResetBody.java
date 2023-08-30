package com.myecommerceapp.espra.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetBody {

    @NotNull @NotBlank
    private String token;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{6,}$")
    private String password;
}
