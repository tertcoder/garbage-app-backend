package com.example.garbageapp.dto;

import com.example.garbageapp.model.SpecialRequest;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialRequestDTO {
    private String id;
    
    private String userId; // This will be set from authentication
    
    @NotBlank(message = "Area ID is required")
    private String areaId;
    
    @NotNull(message = "Request date is required")
    @Future(message = "Request date must be in the future")
    private LocalDate requestDate;
    
    private SpecialRequest.RequestStatus status = SpecialRequest.RequestStatus.PENDING;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;
    
    private String adminNote;
    
    // Optional fields for frontend display
    private String areaName;
    private String userName;
}