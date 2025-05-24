package com.example.garbageapp.controller;

import com.example.garbageapp.dto.ApiResponse;
import com.example.garbageapp.dto.AreaDTO;
import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.service.AreaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/areas")
public class AreaController {

    private final AreaService areaService;
    
    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AreaDTO>> createArea(@Valid @RequestBody AreaDTO areaDTO) {
        AreaDTO createdArea = areaService.createArea(areaDTO);
        return new ResponseEntity<>(
            ApiResponse.success(createdArea, "Area created successfully"),
            HttpStatus.CREATED
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AreaDTO>> getAreaById(@PathVariable String id) {
        AreaDTO area = areaService.getAreaById(id);
        return ResponseEntity.ok(ApiResponse.success(area, "Area retrieved successfully"));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AreaDTO>>> getAllAreas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<AreaDTO> response = areaService.getAllAreas(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Areas retrieved successfully"));
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AreaDTO>>> getAllAreasNoPaging() {
        List<AreaDTO> areas = areaService.getAllAreasNoPaging();
        return ResponseEntity.ok(ApiResponse.success(areas, "All areas retrieved successfully"));
    }
    
    @GetMapping("/zone/{zone}")
    public ResponseEntity<ApiResponse<List<AreaDTO>>> getAreasByZone(@PathVariable String zone) {
        List<AreaDTO> areas = areaService.getAreasByZone(zone);
        return ResponseEntity.ok(ApiResponse.success(areas, "Areas by zone retrieved successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AreaDTO>> updateArea(
            @PathVariable String id,
            @Valid @RequestBody AreaDTO areaDTO
    ) {
        AreaDTO updatedArea = areaService.updateArea(id, areaDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedArea, "Area updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArea(@PathVariable String id) {
        areaService.deleteArea(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Area deleted successfully"));
    }
}