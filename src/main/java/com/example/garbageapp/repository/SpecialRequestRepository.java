package com.example.garbageapp.repository;

import com.example.garbageapp.model.SpecialRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpecialRequestRepository extends MongoRepository<SpecialRequest, String> {
    List<SpecialRequest> findByUserId(String userId);
    Page<SpecialRequest> findByUserId(String userId, Pageable pageable);
    List<SpecialRequest> findByAreaId(String areaId);
    Page<SpecialRequest> findByAreaId(String areaId, Pageable pageable);
    List<SpecialRequest> findByStatus(SpecialRequest.RequestStatus status);
    Page<SpecialRequest> findByStatus(SpecialRequest.RequestStatus status, Pageable pageable);
    List<SpecialRequest> findByRequestDateBetween(LocalDate start, LocalDate end);
    List<SpecialRequest> findByUserIdAndStatus(String userId, SpecialRequest.RequestStatus status);
}