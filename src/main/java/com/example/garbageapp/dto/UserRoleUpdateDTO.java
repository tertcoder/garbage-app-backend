package com.example.garbageapp.dto;

import com.example.garbageapp.model.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UserRoleUpdateDTO {
    @NotEmpty(message = "At least one role is required")
    private Set<Role> roles;
}
