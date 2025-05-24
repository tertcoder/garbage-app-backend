package com.example.garbageapp.controller;

import com.example.garbageapp.dto.ApiResponse;
import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.dto.ScheduleDTO;
import com.example.garbageapp.dto.ScheduleFilterDTO;
import com.example.garbageapp.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleDTO>> createSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        ScheduleDTO createdSchedule = scheduleService.createSchedule(scheduleDTO);
        return new ResponseEntity<>(
            ApiResponse.success(createdSchedule, "Schedule created successfully"),
            HttpStatus.CREATED
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleDTO>> getScheduleById(@PathVariable String id) {
        ScheduleDTO schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(ApiResponse.success(schedule, "Schedule retrieved successfully"));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ScheduleDTO>>> getAllSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<ScheduleDTO> response = scheduleService.getAllSchedules(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedules retrieved successfully"));
    }
    
    @GetMapping("/area/{areaId}")
    public ResponseEntity<ApiResponse<PagedResponse<ScheduleDTO>>> getSchedulesByAreaId(
            @PathVariable String areaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<ScheduleDTO> response = scheduleService.getSchedulesByAreaId(areaId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedules by area retrieved successfully"));
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<ScheduleDTO>>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(schedules, "Schedules by date range retrieved successfully"));
    }
    
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<PagedResponse<ScheduleDTO>>> filterSchedules(
            @RequestBody ScheduleFilterDTO filterDTO
    ) {
        PagedResponse<ScheduleDTO> response = scheduleService.filterSchedules(filterDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Filtered schedules retrieved successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleDTO>> updateSchedule(
            @PathVariable String id,
            @Valid @RequestBody ScheduleDTO scheduleDTO
    ) {
        ScheduleDTO updatedSchedule = scheduleService.updateSchedule(id, scheduleDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedSchedule, "Schedule updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable String id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule deleted successfully"));
    }
}