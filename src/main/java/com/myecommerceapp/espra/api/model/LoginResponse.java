package com.myecommerceapp.espra.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String jwt;
    private boolean success;
    private String failureReason;
}
