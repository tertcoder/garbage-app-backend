package com.example.garbageapp.dto;

import com.example.garbageapp.model.SpecialRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestStatusUpdateDTO {
    
    @NotNull(message = "Status is required")
    private SpecialRequest.RequestStatus status;
    
    @Size(max = 500, message = "Admin note cannot exceed 500 characters")
    private String adminNote;
}