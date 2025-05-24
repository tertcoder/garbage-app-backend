package com.example.garbageapp.service;

import com.example.garbageapp.dto.AreaDTO;
import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.exception.BadRequestException;
import com.example.garbageapp.exception.ResourceNotFoundException;
import com.example.garbageapp.model.Area;
import com.example.garbageapp.repository.AreaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AreaService {

    private final AreaRepository areaRepository;
    
    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }
    
    public AreaDTO createArea(AreaDTO areaDTO) {
        if (areaRepository.existsByName(areaDTO.getName())) {
            throw new BadRequestException("Area with name " + areaDTO.getName() + " already exists");
        }
        
        Area area = new Area();
        area.setName(areaDTO.getName());
        area.setZone(areaDTO.getZone());
        area.setPickupDays(areaDTO.getPickupDays());
        
        Area savedArea = areaRepository.save(area);
        
        return mapToDTO(savedArea);
    }
    
    public AreaDTO getAreaById(String id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + id));
        
        return mapToDTO(area);
    }
    
    public PagedResponse<AreaDTO> getAllAreas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Area> areaPage = areaRepository.findAll(pageable);
        
        List<AreaDTO> content = areaPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                areaPage.getNumber(),
                areaPage.getSize(),
                areaPage.getTotalElements(),
                areaPage.getTotalPages(),
                areaPage.isLast()
        );
    }
    
    public List<AreaDTO> getAllAreasNoPaging() {
        return areaRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AreaDTO> getAreasByZone(String zone) {
        return areaRepository.findByZone(zone)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public AreaDTO updateArea(String id, AreaDTO areaDTO) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + id));
        
        // Check if area name is being changed and new name already exists
        if (!area.getName().equals(areaDTO.getName()) && areaRepository.existsByName(areaDTO.getName())) {
            throw new BadRequestException("Area with name " + areaDTO.getName() + " already exists");
        }
        
        area.setName(areaDTO.getName());
        area.setZone(areaDTO.getZone());
        area.setPickupDays(areaDTO.getPickupDays());
        
        Area updatedArea = areaRepository.save(area);
        
        return mapToDTO(updatedArea);
    }
    
    public void deleteArea(String id) {
        if (!areaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Area not found with id: " + id);
        }
        
        areaRepository.deleteById(id);
    }
    
    private AreaDTO mapToDTO(Area area) {
        AreaDTO areaDTO = new AreaDTO();
        areaDTO.setId(area.getId());
        areaDTO.setName(area.getName());
        areaDTO.setZone(area.getZone());
        areaDTO.setPickupDays(area.getPickupDays());
        return areaDTO;
    }
}