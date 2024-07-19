package com.melodify.Melodify.DTOs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginRequest {
    private String usernameOrEmail;
    private String password;
}
