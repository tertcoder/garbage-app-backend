package com.example.garbageapp.repository;

import com.example.garbageapp.model.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    List<Schedule> findByAreaId(String areaId);
    Page<Schedule> findByAreaId(String areaId, Pageable pageable);
    List<Schedule> findByAreaIdAndType(String areaId, Schedule.ScheduleType type);
    List<Schedule> findByPickupDateBetween(LocalDateTime start, LocalDateTime end);
    List<Schedule> findByAreaIdAndPickupDateBetween(String areaId, LocalDateTime start, LocalDateTime end);
}