package com.example.garbageapp.service;

import com.example.garbageapp.dto.PagedResponse;
import com.example.garbageapp.dto.RequestFilterDTO;
import com.example.garbageapp.dto.RequestStatusUpdateDTO;
import com.example.garbageapp.dto.SpecialRequestDTO;
import com.example.garbageapp.exception.BadRequestException;
import com.example.garbageapp.exception.ResourceNotFoundException;
import com.example.garbageapp.exception.UnauthorizedException;
import com.example.garbageapp.model.SpecialRequest;
import com.example.garbageapp.model.User;
import com.example.garbageapp.repository.AreaRepository;
import com.example.garbageapp.repository.SpecialRequestRepository;
import com.example.garbageapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecialRequestService {

    private final SpecialRequestRepository specialRequestRepository;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    
    public SpecialRequestService(SpecialRequestRepository specialRequestRepository,
                                UserRepository userRepository,
                                AreaRepository areaRepository) {
        this.specialRequestRepository = specialRequestRepository;
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
    }
    
    public SpecialRequestDTO createRequest(SpecialRequestDTO requestDTO) {
        // Get current authenticated user
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        // Verify area exists
        if (!areaRepository.existsById(requestDTO.getAreaId())) {
            throw new ResourceNotFoundException("Area not found with id: " + requestDTO.getAreaId());
        }
        
        // Create new request
        SpecialRequest request = new SpecialRequest();
        request.setUserId(currentUser.getId());
        request.setAreaId(requestDTO.getAreaId());
        request.setRequestDate(requestDTO.getRequestDate());
        request.setStatus(SpecialRequest.RequestStatus.PENDING); // Default status
        request.setDescription(requestDTO.getDescription());
        
        SpecialRequest savedRequest = specialRequestRepository.save(request);
        
        return mapToDTO(savedRequest);
    }
    
    public SpecialRequestDTO getRequestById(String id) {
        SpecialRequest request = specialRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Special request not found with id: " + id));
        
        // Check if user has permission to view this request
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.name().equals("ROLE_ADMIN"));
                
        if (!isAdmin && !request.getUserId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to view this request");
        }
        
        return mapToDTO(request);
    }
    
    public PagedResponse<SpecialRequestDTO> getUserRequests(int page, int size) {
        // Get current authenticated user
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());
        Page<SpecialRequest> requestPage = specialRequestRepository.findByUserId(currentUser.getId(), pageable);
        
        List<SpecialRequestDTO> content = requestPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                requestPage.getNumber(),
                requestPage.getSize(),
                requestPage.getTotalElements(),
                requestPage.getTotalPages(),
                requestPage.isLast()
        );
    }
    
    public PagedResponse<SpecialRequestDTO> getAllRequests(int page, int size) {
        // Check if user is admin
        if (!isCurrentUserAdmin()) {
            throw new UnauthorizedException("You do not have permission to view all requests");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestDate").descending());
        Page<SpecialRequest> requestPage = specialRequestRepository.findAll(pageable);
        
        List<SpecialRequestDTO> content = requestPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                requestPage.getNumber(),
                requestPage.getSize(),
                requestPage.getTotalElements(),
                requestPage.getTotalPages(),
                requestPage.isLast()
        );
    }
    
    public PagedResponse<SpecialRequestDTO> filterRequests(RequestFilterDTO filterDTO) {
        // Check permissions
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        boolean isAdmin = isCurrentUserAdmin();
        
        // Non-admin users can only filter their own requests
        if (!isAdmin) {
            filterDTO.setUserId(currentUser.getId());
        }
        
        Pageable pageable = PageRequest.of(
                filterDTO.getPage(),
                filterDTO.getSize(),
                Sort.by("requestDate").descending()
        );
        
        // Apply filters
        Page<SpecialRequest> requestPage;
        
        if (filterDTO.getUserId() != null) {
            requestPage = specialRequestRepository.findByUserId(filterDTO.getUserId(), pageable);
        } else if (filterDTO.getAreaId() != null) {
            requestPage = specialRequestRepository.findByAreaId(filterDTO.getAreaId(), pageable);
        } else if (filterDTO.getStatus() != null) {
            requestPage = specialRequestRepository.findByStatus(filterDTO.getStatus(), pageable);
        } else {
            requestPage = specialRequestRepository.findAll(pageable);
        }
        
        List<SpecialRequestDTO> content = requestPage.getContent().stream()
                .filter(request -> {
                    // Date range filtering
                    boolean dateFilter = true;
                    if (filterDTO.getStartDate() != null && filterDTO.getEndDate() != null) {
                        dateFilter = request.getRequestDate().isAfter(filterDTO.getStartDate().minusDays(1)) &&
                                    request.getRequestDate().isBefore(filterDTO.getEndDate().plusDays(1));
                    }
                    return dateFilter;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                requestPage.getNumber(),
                requestPage.getSize(),
                content.size(),
                (int) Math.ceil((double) content.size() / filterDTO.getSize()),
                content.size() <= filterDTO.getSize()
        );
    }
    
    public SpecialRequestDTO updateRequestStatus(String id, RequestStatusUpdateDTO statusUpdate) {
        // Check if user is admin
        if (!isCurrentUserAdmin()) {
            throw new UnauthorizedException("You do not have permission to update request status");
        }
        
        SpecialRequest request = specialRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Special request not found with id: " + id));
        
        // Can only update pending requests
        if (request.getStatus() != SpecialRequest.RequestStatus.PENDING) {
            throw new BadRequestException("Cannot update already processed request");
        }
        
        request.setStatus(statusUpdate.getStatus());
        request.setAdminNote(statusUpdate.getAdminNote());
        
        SpecialRequest updatedRequest = specialRequestRepository.save(request);
        
        return mapToDTO(updatedRequest);
    }
    
    public SpecialRequestDTO cancelRequest(String id) {
        // Get current user
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        SpecialRequest request = specialRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Special request not found with id: " + id));
        
        // Verify ownership
        if (!request.getUserId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to cancel this request");
        }
        
        // Can only cancel pending requests
        if (request.getStatus() != SpecialRequest.RequestStatus.PENDING) {
            throw new BadRequestException("Cannot cancel already processed request");
        }
        
        request.setStatus(SpecialRequest.RequestStatus.REJECTED);
        request.setAdminNote("Cancelled by user");
        
        SpecialRequest updatedRequest = specialRequestRepository.save(request);
        
        return mapToDTO(updatedRequest);
    }
    
    public void deleteRequest(String id) {
        // Only admins can delete requests
        if (!isCurrentUserAdmin()) {
            throw new UnauthorizedException("You do not have permission to delete requests");
        }
        
        if (!specialRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Special request not found with id: " + id);
        }
        
        specialRequestRepository.deleteById(id);
    }
    
    private SpecialRequestDTO mapToDTO(SpecialRequest request) {
    SpecialRequestDTO requestDTO = new SpecialRequestDTO();
    requestDTO.setId(request.getId());
    requestDTO.setUserId(request.getUserId());
    requestDTO.setAreaId(request.getAreaId());
    requestDTO.setRequestDate(request.getRequestDate());
    requestDTO.setStatus(request.getStatus());
    requestDTO.setDescription(request.getDescription());
    requestDTO.setAdminNote(request.getAdminNote());
    
    // Fetch area name and user name for display purposes
    areaRepository.findById(request.getAreaId())
            .ifPresent(area -> requestDTO.setAreaName(area.getName()));
            
    userRepository.findById(request.getUserId())
            .ifPresent(user -> requestDTO.setUserName(user.getFullName()));
    
    return requestDTO;
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