package com.example.garbageapp.service;

import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.dto.UserDTO;
import com.example.garbageapp.dto.UserRoleUpdateDTO;
import com.example.garbageapp.exception.ResourceNotFoundException;
import com.example.garbageapp.exception.UnauthorizedException;
import com.example.garbageapp.model.User;
import com.example.garbageapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getUserById(String id) {
        checkAdminAccess();
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return mapToDTO(user);
    }
    
    public UserDTO getCurrentUser() {
        String currentUserEmail = getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return mapToDTO(user);
    }
    
    public PagedResponse<UserDTO> getAllUsers(int page, int size) {
        checkAdminAccess();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserDTO> content = userPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }
    
    public UserDTO updateUserRoles(String id, UserRoleUpdateDTO roleUpdateDTO) {
        checkAdminAccess();
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setRoles(roleUpdateDTO.getRoles());
        User updatedUser = userRepository.save(user);
        
        return mapToDTO(updatedUser);
    }
    
    public UserDTO toggleUserActive(String id, boolean active) {
        checkAdminAccess();
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setActive(active);
        User updatedUser = userRepository.save(user);
        
        return mapToDTO(updatedUser);
    }
    
    private UserDTO mapToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRoles(user.getRoles());
        userDTO.setActive(user.isActive());
        userDTO.setCreatedAt(user.getCreatedAt());
        return userDTO;
    }
    
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    
    private void checkAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            throw new UnauthorizedException("Admin access required");
        }
    }
}