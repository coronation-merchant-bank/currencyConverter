package com.cmb.efx.service;

import com.cmb.efx.dto.*;
import com.cmb.efx.entity.GlobalVolumeLimit;
import com.cmb.efx.entity.Status;
import com.cmb.efx.entity.Transactions;
import com.cmb.efx.repository.TransactionRepository;
import com.cmb.efx.repository.VolumeRepository;
import com.cmb.efx.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolumeService {

    private final VolumeRepository volumeRepository;
    private final TokenService tokenService;
    private final TransactionRepository transactionRepository;
    private final DataSource dataSource;

    private static final String INVALID_TOKEN = "Invalid token";
    private static final String SUCCESS = "Success";

    public Response saveVolumeLimit(CreateVolumeLimitRequest request, String token){
        log.debug("Start date: {}", request.getStartDate());
        log.debug("Start Time: {}", request.getStartTime());
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }

        String username = tokenService.getUsername(token);

        LocalDateTime startDate = LocalDateTime.of(LocalDate.parse(request.getStartDate()), LocalTime.parse(request.getStartTime()));
        LocalDateTime endDate = LocalDateTime.of(LocalDate.parse(request.getEndDate()), LocalTime.parse(request.getEndTime()));

        GlobalVolumeLimit globalVolumeLimit = GlobalVolumeLimit.builder()
                .universalGlobalLimit(request.getUniversalGlobalLimit())
                .currencyType(request.getCurrencyType())
                .startDate(startDate)
                .endDate(endDate)
                .initiator(username)
                .status(Status.PENDING)
                .usedFlag("UNUSED")
                .build();

        volumeRepository.save(globalVolumeLimit);

        return Response.builder()
                .responseMessage(SUCCESS)
                .build();
    }


    public Response fetchVolume(int pageNumber, int pageSize, String fromDate, String toDate,
                                String currencyType, String status, String searchQuery, String token) {

        if (tokenService.validateToken(token)) {
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("id").descending());

        Specification<GlobalVolumeLimit> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Filter by date range if provided
            if (fromDate != null && toDate != null) {
                LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
                LocalDateTime to = LocalDate.parse(toDate).atTime(23, 59, 59, 999999999);
                if (from.isAfter(to)) {
                    LocalDateTime temp = from;
                    from = to;
                    to = temp;
                }
                predicates.add(cb.between(root.get("createdAt"), from, to));
            }

            // Filter by currencyType if provided
            if (currencyType != null && !currencyType.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("currencyType")), currencyType.toLowerCase()));
            }

            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }

            // Dynamic searching across multiple fields
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String likePattern = "%" + searchQuery.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("currencyType")), likePattern),
                        cb.like(cb.lower(root.get("status")), likePattern),
                        cb.like(cb.lower(root.get("authorizer")), likePattern),
                        cb.like(cb.lower(root.get("initiator")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<GlobalVolumeLimit> pageList = volumeRepository.findAll(spec, pageable);
        List<VolumeLimitResponse> volumeLimitResponses = pageList.getContent()
                .stream()
                .map(Mapper::mapGlvToVolumeLimitResponse)
                .toList();

        return Response.builder()
                .responseMessage(SUCCESS)
                .volumeLimitList(volumeLimitResponses)
                .totalElements((int) pageList.getTotalElements())
                .build();
    }

    public boolean isThereValidVolume(String currencyType){
        // Fetch approved volume limits where endDate is not elapsed
        List<GlobalVolumeLimit> volumeLimits = volumeRepository.findAll().stream()
                .filter(limit -> currencyType.contains(limit.getCurrencyType()))  // Ensure it's one of the 3 currencies
                .filter(limit -> limit.getAuthorizer() != null)  // Ensure it has been authorized
                .filter(limit -> limit.getStatus().toString().equalsIgnoreCase("APPROVED"))  // Ensure it's approved
                .filter(limit -> limit.getEndDate().isAfter(LocalDateTime.now()))  // Ensure endDate is in the future
                .toList();

        return volumeLimits.isEmpty();
    }


    public Response fetchCurrentVolumeLimit(String token) {
        // Validate Token
        if (tokenService.validateToken(token)) {
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }

        List<GlobalVolumeLimit> volumeLimits = new ArrayList<>();
        List<VolumeLimitResponse> volumeLimitResponseList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall("{CALL CMB_FetchActiveLimit()}")) {

            boolean hasResults = callableStatement.execute();
            if (hasResults) {
                try (ResultSet resultSet = callableStatement.getResultSet()) {
                    while (resultSet.next()) {
                        GlobalVolumeLimit limit = new GlobalVolumeLimit();
                        limit.setCurrencyType(resultSet.getString("currencyType"));
                        limit.setAuthorizer(resultSet.getString("authorizer"));
                        limit.setStatus(Status.valueOf(resultSet.getString("status")));
                        limit.setEndDate(resultSet.getTimestamp("endDate").toLocalDateTime());
                        limit.setUsedFlag(resultSet.getString("usedFlag"));
                        limit.setApprovedDate(resultSet.getTimestamp("approvedDate").toLocalDateTime());
                        limit.setStartDate(resultSet.getTimestamp("startDate").toLocalDateTime());
                        limit.setUsedVolume(resultSet.getDouble("usedVolume"));
                        limit.setCurrencyType(resultSet.getString("currencyType"));
                        limit.setCreatedAt(resultSet.getTimestamp("createdAt").toLocalDateTime());
                        limit.setModifiedAt(resultSet.getTimestamp("modifiedAt").toLocalDateTime());
                        limit.setInitiator(resultSet.getString("initiator"));
                        limit.setAuthorizer(resultSet.getString("authorizer"));
                        limit.setRejectDate(resultSet.getTimestamp("rejectDate").toLocalDateTime());
                        limit.setUsedFlag(resultSet.getString("usedFlag"));
                        // Set other fields as necessary
                        volumeLimits.add(limit);
                    }
                }
            }
            volumeLimitResponseList = volumeLimits.stream().map(Mapper::mapGlvToVolumeLimitResponse).toList();
        } catch (SQLException e) {
            return Response.builder()
                    .responseMessage("Error executing stored procedure")
                    .build();
        }

        return Response.builder()
                .responseMessage(SUCCESS)
                .volumeLimitList(volumeLimitResponseList)
                .build();
    }


    public Response approveVolume(ApproveVolumeRequest request, String token) {
        // Validate the token
        if (tokenService.validateToken(token)) {
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }

        String username = tokenService.getUsername(token);

        // Check if the volume exists
        Optional<GlobalVolumeLimit> checkVolume = volumeRepository.findById(request.getVolumeId());
        if (checkVolume.isEmpty()) {
            return Response.builder()
                    .responseMessage("Record with id " + request.getVolumeId() + " does not exist")
                    .build();
        }

        GlobalVolumeLimit volume = checkVolume.get();

        // Check if the volume is expired
        if (volume.getEndDate().isBefore(LocalDateTime.now())) {
            return Response.builder()
                    .responseMessage("Volume is already expired")
                    .build();
        }

        if (volume.getStatus().equals(Status.APPROVED) || volume.getStatus().equals(Status.REJECTED) || volume.getStatus().equals(Status.INACTIVE)) {
            return Response.builder()
                    .responseMessage("Volume has reached a terminal stage and cannot be approved")
                    .build();
        }

        // Find all currently approved volumes of the same currency type
        List<GlobalVolumeLimit> existingApprovedVolumes = volumeRepository.findByCurrencyTypeAndStatus(
                volume.getCurrencyType(), Status.APPROVED);

        // Set them to STOPPED
        for (GlobalVolumeLimit approvedVolume : existingApprovedVolumes) {
            approvedVolume.setStatus(Status.INACTIVE);
            approvedVolume.setUsedFlag("USED"); // If applicable
        }

        // Save updated volumes
        if (!existingApprovedVolumes.isEmpty()) {
            volumeRepository.saveAll(existingApprovedVolumes);
        }

        // Approve the new volume
        volume.setStatus(Status.APPROVED);
        volume.setAuthorizer(username);
        volume.setApprovedDate(LocalDateTime.now());
        volumeRepository.save(volume);

        return Response.builder()
                .responseMessage("Volume has been successfully updated")
                .build();
    }

    public Response rejectVolume(ApproveVolumeRequest request, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }

        String username = tokenService.getUsername(token);

        Optional<GlobalVolumeLimit> checkVolume = volumeRepository.findById(request.getVolumeId());
        if (checkVolume.isEmpty()){
            return Response.builder()
                    .responseMessage("Record with id " + request.getVolumeId() + " does not exist")
                    .build();
        }

        GlobalVolumeLimit volume = checkVolume.get();
        volume.setStatus(Status.REJECTED);
        volume.setAuthorizer(username);
        volume.setRejectDate(LocalDateTime.now());
        volumeRepository.save(volume);
        return Response.builder()
                .responseMessage("Volume has been successfully updated")
                .build();
    }
}

