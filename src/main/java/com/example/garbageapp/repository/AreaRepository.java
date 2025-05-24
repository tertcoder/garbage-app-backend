package com.example.garbageapp.repository;

import com.example.garbageapp.model.Area;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends MongoRepository<Area, String> {
    Optional<Area> findByName(String name);
    List<Area> findByZone(String zone);
    boolean existsByName(String name);
}