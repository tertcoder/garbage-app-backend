package com.example.garbageapp.controller;

import com.example.garbageapp.dto.*;
import com.example.garbageapp.service.SpecialRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/special-requests")
public class SpecialRequestController {

    private final SpecialRequestService specialRequestService;
    
    public SpecialRequestController(SpecialRequestService specialRequestService) {
        this.specialRequestService = specialRequestService;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SpecialRequestDTO>> createRequest(
            @Valid @RequestBody SpecialRequestDTO requestDTO
    ) {
        SpecialRequestDTO createdRequest = specialRequestService.createRequest(requestDTO);
        return new ResponseEntity<>(
            ApiResponse.success(createdRequest, "Special request created successfully"),
            HttpStatus.CREATED
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialRequestDTO>> getRequestById(@PathVariable String id) {
        SpecialRequestDTO request = specialRequestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(request, "Special request retrieved successfully"));
    }
    
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<PagedResponse<SpecialRequestDTO>>> getUserRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<SpecialRequestDTO> response = specialRequestService.getUserRequests(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "User's special requests retrieved successfully"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<SpecialRequestDTO>>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<SpecialRequestDTO> response = specialRequestService.getAllRequests(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "All special requests retrieved successfully"));
    }
    
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<PagedResponse<SpecialRequestDTO>>> filterRequests(
            @RequestBody RequestFilterDTO filterDTO
    ) {
        PagedResponse<SpecialRequestDTO> response = specialRequestService.filterRequests(filterDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Filtered special requests retrieved successfully"));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SpecialRequestDTO>> updateRequestStatus(
            @PathVariable String id,
            @Valid @RequestBody RequestStatusUpdateDTO statusUpdate
    ) {
        SpecialRequestDTO updatedRequest = specialRequestService.updateRequestStatus(id, statusUpdate);
        return ResponseEntity.ok(ApiResponse.success(updatedRequest, "Special request status updated successfully"));
    }
    
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SpecialRequestDTO>> cancelRequest(@PathVariable String id) {
        SpecialRequestDTO cancelledRequest = specialRequestService.cancelRequest(id);
        return ResponseEntity.ok(ApiResponse.success(cancelledRequest, "Special request cancelled successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable String id) {
        specialRequestService.deleteRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Special request deleted successfully"));
    }
}