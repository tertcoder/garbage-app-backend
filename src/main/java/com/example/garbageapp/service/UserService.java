package com.example.garbageapp.service;

import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.dto.UserDTO;
import com.example.garbageapp.dto.UserRoleUpdateDTO;
import com.example.garbageapp.exception.BadRequestException;
import com.example.garbageapp.exception.ResourceNotFoundException;
import com.example.garbageapp.exception.UnauthorizedException;
import com.example.garbageapp.model.Role;
import com.example.garbageapp.model.User;
import com.example.garbageapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO getUserById(String id) {
        checkAdminAccess();
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return mapToDTO(user);
    }
    
    public UserDTO getCurrentUser() {
        try {
            String currentUserEmail = getCurrentUserEmail();
            if (currentUserEmail == null) {
                throw new UnauthorizedException("No authenticated user found");
            }
            
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));
            
            return mapToDTO(user);
        } catch (Exception e) {
            logger.error("Error in getCurrentUser: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public PagedResponse<UserDTO> getAllUsers(int page, int size) {
        checkAdminAccess();
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        var userPage = userRepository.findAll(pageable);
        
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
    
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }

        User user = new User();
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRoles(userDTO.getRoles());
        user.setActive(true);

        return mapToDTO(userRepository.save(user));
    }
    
    public UserDTO updateUser(String id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }

        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        user.setPhoneNumber(userDTO.getPhoneNumber());

        return mapToDTO(userRepository.save(user));
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