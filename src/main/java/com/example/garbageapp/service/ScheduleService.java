package com.example.garbageapp.service;

import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.dto.ScheduleDTO;
import com.example.garbageapp.dto.ScheduleFilterDTO;
import com.example.garbageapp.exception.BadRequestException;
import com.example.garbageapp.exception.ResourceNotFoundException;
import com.example.garbageapp.model.Area;
import com.example.garbageapp.model.Schedule;
import com.example.garbageapp.repository.AreaRepository;
import com.example.garbageapp.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final AreaRepository areaRepository;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    
    public ScheduleService(ScheduleRepository scheduleRepository, AreaRepository areaRepository) {
        this.scheduleRepository = scheduleRepository;
        this.areaRepository = areaRepository;
    }
    
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO) {
        try {
            logger.info("Creating schedule with data: {}", scheduleDTO);
            
            // Verify area exists
            if (!areaRepository.existsById(scheduleDTO.getAreaId())) {
                logger.error("Area not found with id: {}", scheduleDTO.getAreaId());
                throw new ResourceNotFoundException("Area not found with id: " + scheduleDTO.getAreaId());
            }
            
            // Validate pickup date
            LocalDateTime now = LocalDateTime.now();
            if (scheduleDTO.getPickupDate().isBefore(now)) {
                logger.error("Invalid pickup date: {} is not in the future", scheduleDTO.getPickupDate());
                throw new BadRequestException("Pickup date must be in the future");
            }
            
            Schedule schedule = new Schedule();
            schedule.setAreaId(scheduleDTO.getAreaId());
            schedule.setPickupDate(scheduleDTO.getPickupDate());
            schedule.setType(scheduleDTO.getType());
            schedule.setNotes(scheduleDTO.getNotes());
            
            logger.info("Saving schedule: {}", schedule);
            Schedule savedSchedule = scheduleRepository.save(schedule);
            logger.info("Schedule saved successfully with id: {}", savedSchedule.getId());
            
            return mapToDTO(savedSchedule);
        } catch (Exception e) {
            logger.error("Error creating schedule: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public ScheduleDTO getScheduleById(String id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        
        return mapToDTO(schedule);
    }
    
    public PagedResponse<ScheduleDTO> getAllSchedules(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("pickupDate").ascending());
        Page<Schedule> schedulePage = scheduleRepository.findAll(pageable);
        
        List<ScheduleDTO> content = schedulePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                schedulePage.getNumber(),
                schedulePage.getSize(),
                schedulePage.getTotalElements(),
                schedulePage.getTotalPages(),
                schedulePage.isLast()
        );
    }
    
    public PagedResponse<ScheduleDTO> getSchedulesByAreaId(String areaId, int page, int size) {
        // Verify area exists
        if (!areaRepository.existsById(areaId)) {
            throw new ResourceNotFoundException("Area not found with id: " + areaId);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("pickupDate").ascending());
        Page<Schedule> schedulePage = scheduleRepository.findByAreaId(areaId, pageable);
        
        List<ScheduleDTO> content = schedulePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                schedulePage.getNumber(),
                schedulePage.getSize(),
                schedulePage.getTotalElements(),
                schedulePage.getTotalPages(),
                schedulePage.isLast()
        );
    }
    
    public List<ScheduleDTO> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        return scheduleRepository.findByPickupDateBetween(startDateTime, endDateTime)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public PagedResponse<ScheduleDTO> filterSchedules(ScheduleFilterDTO filterDTO) {
        Pageable pageable = PageRequest.of(
                filterDTO.getPage(), 
                filterDTO.getSize(), 
                Sort.by("pickupDate").ascending()
        );
        
        // Convert dates to LocalDateTime for filtering
        LocalDateTime startDateTime = filterDTO.getStartDate() != null ? 
                filterDTO.getStartDate().atStartOfDay() : LocalDate.now().atStartOfDay();
        
        LocalDateTime endDateTime = filterDTO.getEndDate() != null ?
                filterDTO.getEndDate().atTime(LocalTime.MAX) : LocalDate.now().plusMonths(1).atTime(LocalTime.MAX);
        
        // Get schedules by area and date range
        Page<Schedule> schedulePage;
        if (filterDTO.getAreaId() != null) {
            schedulePage = scheduleRepository.findByAreaId(filterDTO.getAreaId(), pageable);
        } else {
            schedulePage = scheduleRepository.findAll(pageable);
        }
        
        List<ScheduleDTO> content = schedulePage.getContent().stream()
                .filter(schedule -> (filterDTO.getType() == null || schedule.getType() == filterDTO.getType()))
                .filter(schedule -> schedule.getPickupDate().isAfter(startDateTime) && 
                        schedule.getPickupDate().isBefore(endDateTime))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                schedulePage.getNumber(),
                schedulePage.getSize(),
                content.size(), // This is not accurate for total elements but works for simple filtering
                (int) Math.ceil((double) content.size() / filterDTO.getSize()),
                content.size() <= filterDTO.getSize()
        );
    }
    
    public ScheduleDTO updateSchedule(String id, ScheduleDTO scheduleDTO) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        
        // Verify area exists if it's being changed
        if (!schedule.getAreaId().equals(scheduleDTO.getAreaId()) && 
                !areaRepository.existsById(scheduleDTO.getAreaId())) {
            throw new ResourceNotFoundException("Area not found with id: " + scheduleDTO.getAreaId());
        }
        
        schedule.setAreaId(scheduleDTO.getAreaId());
        schedule.setPickupDate(scheduleDTO.getPickupDate());
        schedule.setType(scheduleDTO.getType());
        
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        
        return mapToDTO(updatedSchedule);
    }
    
    public void deleteSchedule(String id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }
        
        scheduleRepository.deleteById(id);
    }
    
    private ScheduleDTO mapToDTO(Schedule schedule) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setId(schedule.getId());
        scheduleDTO.setAreaId(schedule.getAreaId());
        scheduleDTO.setPickupDate(schedule.getPickupDate());
        scheduleDTO.setType(schedule.getType());
        scheduleDTO.setNotes(schedule.getNotes());
        
        // Fetch area name for display purposes
        areaRepository.findById(schedule.getAreaId())
                .ifPresent(area -> scheduleDTO.setAreaName(area.getName()));
        
        return scheduleDTO;
    }
}