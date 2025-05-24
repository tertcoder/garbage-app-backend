package com.example.garbageapp.dto;

import com.example.garbageapp.model.Schedule;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private String id;
    
    @NotBlank(message = "Area ID is required")
    private String areaId;
    
    @NotNull(message = "Pickup date is required")
    @Future(message = "Pickup date must be in the future")
    private LocalDateTime pickupDate;
    
    @NotNull(message = "Schedule type is required")
    private Schedule.ScheduleType type;
    
    private String areaName; // Optional field for frontend display
}