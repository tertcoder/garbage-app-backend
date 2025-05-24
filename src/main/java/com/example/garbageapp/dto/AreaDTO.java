package com.example.garbageapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaDTO {
    private String id;
    
    @NotBlank(message = "Area name is required")
    private String name;
    
    @NotBlank(message = "Zone is required")
    private String zone;
    
    @NotEmpty(message = "At least one pickup day is required")
    private List<String> pickupDays;
}