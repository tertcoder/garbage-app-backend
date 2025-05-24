package com.example.garbageapp.service;

import com.example.garbageapp.dto.DashboardStatsDTO;
import com.example.garbageapp.exception.UnauthorizedException;
import com.example.garbageapp.model.SpecialRequest;
import com.example.garbageapp.repository.AreaRepository;
import com.example.garbageapp.repository.ScheduleRepository;
import com.example.garbageapp.repository.SpecialRequestRepository;
import com.example.garbageapp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final ScheduleRepository scheduleRepository;
    private final SpecialRequestRepository specialRequestRepository;
    
    public StatisticsService(UserRepository userRepository,
                             AreaRepository areaRepository,
                             ScheduleRepository scheduleRepository,
                             SpecialRequestRepository specialRequestRepository) {
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
        this.scheduleRepository = scheduleRepository;
        this.specialRequestRepository = specialRequestRepository;
    }
    
    public DashboardStatsDTO getAdminDashboardStats() {
        // Check if user is admin
        if (!isCurrentUserAdmin()) {
            throw new UnauthorizedException("Admin access required");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekLater = now.plusWeeks(1);
        
        // Count totals
        long totalUsers = userRepository.count();
        long totalAreas = areaRepository.count();
        long totalRequests = specialRequestRepository.count();
        
        // Count pending requests
        long pendingRequests = specialRequestRepository.findByStatus(SpecialRequest.RequestStatus.PENDING).size();
        
        // Count upcoming collections
        long upcomingCollections = scheduleRepository.findByPickupDateBetween(now, oneWeekLater).size();
        
        // Get requests by status
        Map<String, Long> requestsByStatus = specialRequestRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    request -> request.getStatus().name(),
                    Collectors.counting()
                ));
        
        // Get requests by zone (requires multiple queries)
        Map<String, Long> requestsByZone = new HashMap<>();
        specialRequestRepository.findAll().forEach(request -> {
            areaRepository.findById(request.getAreaId()).ifPresent(area -> {
                String zone = area.getZone();
                requestsByZone.put(zone, requestsByZone.getOrDefault(zone, 0L) + 1);
            });
        });
        
        // Build and return statistics
        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalAreas(totalAreas)
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .upcomingCollections(upcomingCollections)
                .requestsByStatus(requestsByStatus)
                .requestsByZone(requestsByZone)
                .build();
    }
    
    public DashboardStatsDTO getUserDashboardStats() {
        // Get current user id
        String currentUserEmail = getCurrentUserEmail();
        
        // Get date ranges
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekLater = now.plusWeeks(1);
        
        // Get user's requests
        var userRequests = specialRequestRepository.findByUserIdAndStatus(
                userRepository.findByEmail(currentUserEmail).orElseThrow().getId(),
                SpecialRequest.RequestStatus.PENDING
        );
        
        long pendingRequests = userRequests.size();
        long upcomingCollections = scheduleRepository.findByPickupDateBetween(now, oneWeekLater).size();
        
        // Build and return limited statistics for user
        return DashboardStatsDTO.builder()
                .pendingRequests(pendingRequests)
                .upcomingCollections(upcomingCollections)
                .build();
    }
    
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}