package com.example.garbageapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "special_requests")
public class SpecialRequest {
    
    @Id
    private String id;
    
    private String userId;
    
    private String areaId;
    
    private LocalDate requestDate;
    
    private RequestStatus status;
    
    private String description;
    
    private String adminNote;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}