package com.myecommerceapp.espra.api.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationBody {

    @NotNull @NotBlank
    @Size(min = 3, max = 15)
    private String username;
    @NotNull @NotBlank
    @Email
    private String email;
    @NotNull @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{6,}$")
    private String password;
    @NotNull @NotBlank
    private String firstName;
    @NotNull @NotBlank
    private String lastName;
}
