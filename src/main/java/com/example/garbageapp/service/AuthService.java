package com.example.garbageapp.service;

import com.example.garbageapp.dto.AuthResponse;
import com.example.garbageapp.dto.LoginRequest;
import com.example.garbageapp.dto.RegisterRequest;
import com.example.garbageapp.exception.BadRequestException;
import com.example.garbageapp.model.Role;
import com.example.garbageapp.model.User;
import com.example.garbageapp.repository.UserRepository;
import com.example.garbageapp.security.JwtTokenProvider;
import com.example.garbageapp.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }
    
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }
        
        // Check if phone number exists
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered");
        }
        
        // Create new user
        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRoles(Collections.singleton(Role.ROLE_USER)); // Default role
        
        User savedUser = userRepository.save(user);
        
        // Generate tokens
        String token = tokenProvider.generateToken(savedUser.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getEmail());
        
        return AuthResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }
    
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails.getUsername());
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        return AuthResponse.builder()
                .id(userDetails.getId())
                .fullName(userDetails.getFullName())
                .email(userDetails.getUsername())
                .roles(user.getRoles())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }
}