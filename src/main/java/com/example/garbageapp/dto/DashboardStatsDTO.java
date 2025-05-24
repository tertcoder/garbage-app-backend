package com.example.garbageapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalAreas;
    private long totalRequests;
    private long pendingRequests;
    private long upcomingCollections;
    private Map<String, Long> requestsByStatus;
    private Map<String, Long> requestsByZone;
}