package com.example.garbageapp.dto;

import com.example.garbageapp.model.Schedule;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleFilterDTO {
    private String areaId;
    private Schedule.ScheduleType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 0;
    private Integer size = 10;
}