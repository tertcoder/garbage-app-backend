package com.example.garbageapp.dto;

import com.example.garbageapp.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String id;
    private String fullName;
    private String email;
    private Set<Role> roles;
    private String token;
    private String refreshToken;
}