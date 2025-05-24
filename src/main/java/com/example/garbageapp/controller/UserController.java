package com.example.garbageapp.controller;

import com.example.garbageapp.dto.ApiResponse;
import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.dto.UserDTO;
import com.example.garbageapp.dto.UserRoleUpdateDTO;
import com.example.garbageapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "Current user retrieved successfully"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable String id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<UserDTO> response = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }
    
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRoles(
            @PathVariable String id,
            @Valid @RequestBody UserRoleUpdateDTO roleUpdateDTO
    ) {
        UserDTO updatedUser = userService.updateUserRoles(id, roleUpdateDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User roles updated successfully"));
    }
    
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserActive(
            @PathVariable String id,
            @RequestParam boolean active
    ) {
        UserDTO updatedUser = userService.toggleUserActive(id, active);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User active status updated successfully"));
    }
}