package com.example.garbageapp.dto;

import com.example.garbageapp.model.SpecialRequest;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestFilterDTO {
    private String userId;
    private String areaId; 
    private SpecialRequest.RequestStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 0;
    private Integer size = 10;
}