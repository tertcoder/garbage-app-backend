package com.example.garbageapp.controller;

import com.example.garbageapp.dto.ApiResponse;
import com.example.garbageapp.dto.DashboardStatsDTO;
import com.example.garbageapp.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final StatisticsService statisticsService;
    
    public DashboardController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getAdminDashboardStats() {
        DashboardStatsDTO stats = statisticsService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Admin dashboard statistics retrieved successfully"));
    }
    
    @GetMapping("/user/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getUserDashboardStats() {
        DashboardStatsDTO stats = statisticsService.getUserDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "User dashboard statistics retrieved successfully"));
    }
}