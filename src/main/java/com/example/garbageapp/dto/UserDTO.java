package com.example.garbageapp.dto;

import com.example.garbageapp.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private Set<Role> roles;
    private boolean active;
    private LocalDateTime createdAt;
}
 

