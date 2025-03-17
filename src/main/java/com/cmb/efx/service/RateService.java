package com.cmb.efx.service;


import com.cmb.efx.dto.ApproveRateRequest;
import com.cmb.efx.dto.CreateRateRequest;
import com.cmb.efx.dto.Response;
import com.cmb.efx.entity.Rates;
import com.cmb.efx.entity.Status;
import com.cmb.efx.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateService {
    private final RateRepository rateRepository;
    private final TokenService tokenService;
    private static final String INVALID_TOKEN = "Invalid Token";
    private final DataSource dataSource;

    public Response createRate(CreateRateRequest request, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }

        String username = tokenService.getUsername(token);
        log.info("Username: {}", username);

        System.out.println("User is: " + username);

        Rates rates = Rates.builder()
                .usdOffer(request.getUsdOffer())
                .gbpOffer(request.getGbpOffer())
                .eurOffer(request.getEurOffer())
                .usdBid(request.getUsdBid())
                .gbpBid(request.getGbpBid())
                .eurBid(request.getEurBid())
                .status(Status.PENDING)
                .initiator(tokenService.getUsername(token))
                .build();

        rateRepository.save(rates);

        return Response.builder()
                .responseMessage("SUCCESS")
                .rates(request)
                .build();
    }

    public Response updateRate(CreateRateRequest request, Long rateId, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage(INVALID_TOKEN)
                    .build();
        }
        Optional<Rates> existingRate = rateRepository.findById(rateId);
        if (existingRate.isEmpty()){
            return Response.builder()
                    .responseMessage("Rate with id " + rateId + " does not exist")
                    .build();
        }

        existingRate.get().setUsdBid(request.getUsdBid());
        existingRate.get().setGbpBid(request.getGbpBid());
        existingRate.get().setEurBid(request.getEurBid());
        existingRate.get().setUsdOffer(request.getUsdOffer());
        existingRate.get().setGbpOffer(request.getGbpOffer());
        existingRate.get().setEurOffer(request.getEurOffer());

        rateRepository.save(existingRate.get());

        return Response.builder()
                .responseMessage("Success")
                .rate(existingRate.get())
                .build();
    }

    public Response fetchRateById(Long rateId, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        Rates rate = new Rates();
        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall("{CALL CMB_FetchRatesByID(?)}")) {

            callableStatement.setLong(1, rateId);
            ResultSet resultSet = callableStatement.executeQuery();

            if (!resultSet.next()) {
                return Response.builder()
                        .responseMessage("Rate with id " + rateId + " does not exist")
                        .build();
            }

            rate.setId(resultSet.getLong("id"));
            rate.setId(resultSet.getLong("id"));
            rate.setUsdBid(resultSet.getDouble("usdBid"));
            rate.setGbpBid(resultSet.getDouble("gbpBid"));
            rate.setEurBid(resultSet.getDouble("eurBid"));
            rate.setUsdOffer(resultSet.getDouble("usdOffer"));
            rate.setGbpOffer(resultSet.getDouble("gbpOffer"));
            rate.setEurOffer(resultSet.getDouble("eurOffer"));
            rate.setStatus(Status.valueOf(resultSet.getString("status")));
            rate.setInitiator(resultSet.getString("initiator"));
            rate.setAuthorizer(resultSet.getString("Authorizer"));
            rate.setCreatedAt(resultSet.getTimestamp("createdAt").toLocalDateTime());
            rate.setUpdatedAt(resultSet.getTimestamp("updatedAt").toLocalDateTime());
            rate.setApproveDate(resultSet.getTimestamp("approveDate").toLocalDateTime());
            rate.setRejectDate(resultSet.getTimestamp("rejectDate").toLocalDateTime());



        } catch (SQLException e) {
            e.printStackTrace();
            return Response.builder()
                    .responseMessage("Error fetching rate")
                    .build();
        }
        return Response.builder()
                .responseMessage("Success")
                .rate(rate)
                .build();
    }

    public Response fetchRates(int pageNumber, int pageSize, String fromDate, String toDate, String token) {
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("id").descending());

        // Check if date filters are present
        Page<Rates> pageList;
        if (fromDate != null && toDate != null) {
            // Parse fromDate and toDate, creating full-day LocalDateTime range
            LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
            LocalDateTime to = LocalDate.parse(toDate).atTime(23, 59, 59, 999999999);

            // Swap if fromDate > toDate
            if (from.isAfter(to)) {
                LocalDateTime temp = from;
                from = to;
                to = temp;
            }

            // Call the repository method with LocalDateTime
            pageList = rateRepository.findByApproveDateBetween(from, to, pageable);
        } else {
            pageList = rateRepository.findAll(pageable);
        }


        return Response.builder()
                .responseMessage("Success")
                .ratesList(pageList.getContent())
                .totalElements((int) pageList.getTotalElements())
                .build();
    }

    public Response currentRate(String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        Rates currentRate = null;
        List<Rates> ratesList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall("{CALL CMB_FetchActiveRate()}")) {

            boolean hasResults = callableStatement.execute();
            if (hasResults) {
                try (ResultSet resultSet = callableStatement.getResultSet()) {
                    if (resultSet.next()) {
                        currentRate = new Rates();
                        currentRate.setId(resultSet.getLong("id"));
                        currentRate.setUsdBid(resultSet.getDouble("usdBid"));
                        currentRate.setGbpBid(resultSet.getDouble("gbpBid"));
                        currentRate.setEurBid(resultSet.getDouble("eurBid"));
                        currentRate.setUsdOffer(resultSet.getDouble("usdOffer"));
                        currentRate.setGbpOffer(resultSet.getDouble("gbpOffer"));
                        currentRate.setEurOffer(resultSet.getDouble("eurOffer"));
                        currentRate.setStatus(Status.valueOf(resultSet.getString("status")));
                        currentRate.setInitiator(resultSet.getString("initiator"));
                        currentRate.setAuthorizer(resultSet.getString("Authorizer"));
                        currentRate.setCreatedAt(resultSet.getTimestamp("createdAt").toLocalDateTime());
                        currentRate.setUpdatedAt(resultSet.getTimestamp("updatedAt").toLocalDateTime());
                        currentRate.setApproveDate(resultSet.getTimestamp("approveDate").toLocalDateTime());
                        currentRate.setRejectDate(resultSet.getTimestamp("rejectDate").toLocalDateTime());
                        ratesList.add(currentRate);
                    }
                }
            }
        } catch (SQLException e) {
            return Response.builder()
                    .responseMessage("Error executing stored procedure")
                    .build();
        }
        if (ratesList.isEmpty()){
            return Response.builder()
                    .responseMessage("there's no current rate in use")
                    .build();
        }
        Rates activeRate = ratesList.getLast();

        return Response.builder()
                .responseMessage("Success")
                .rate(activeRate)
                .build();
    }

    @Transactional
    public Response approveRate(ApproveRateRequest request, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        Optional<Rates> rates = rateRepository.findById(request.getRateId());
        if (rates.isEmpty()){
            return Response.builder()
                    .responseMessage("Rate with id " + request.getRateId() + " does not exist")
                    .build();
        }

        rateRepository.findAll().stream().filter(rate -> rate.getStatus().equals(Status.APPROVED))
                        .forEach(rate -> {
                            rate.setStatus(Status.INACTIVE);
                            rateRepository.save(rate);
                        });

        Rates rateToUpdate = rates.get();
        if (rateToUpdate.getStatus() == Status.APPROVED || rateToUpdate.getStatus() == Status.INACTIVE || rateToUpdate.getStatus() == Status.REJECTED){
            return Response.builder()
                    .responseMessage("Rate has reached a terminal stage and may not be approved")
                    .build();
        }
        rateToUpdate.setStatus(Status.APPROVED);
        rateToUpdate.setApproveDate(LocalDateTime.now());
        rateToUpdate.setAuthorizer(tokenService.getUsername(token));
        rateRepository.save(rateToUpdate);

        return Response.builder()
                .responseMessage("Success")
                .rate(rateToUpdate)
                .build();
    }

    public Response rejectRate(ApproveRateRequest request, String token){
        if (tokenService.validateToken(token)){
            return Response.builder()
                    .responseMessage("Invalid Token")
                    .build();
        }
        Optional<Rates> rates = rateRepository.findById(request.getRateId());
        if (rates.isEmpty()){
            return Response.builder()
                    .responseMessage("Rate with id " + request.getRateId() + " does not exist")
                    .build();
        }
        Rates rateToUpdate = rates.get();
        rateToUpdate.setStatus(Status.REJECTED);
        rateToUpdate.setRejectDate(LocalDateTime.now());
        rateToUpdate.setAuthorizer(request.getAuthorizer());
        rateRepository.save(rateToUpdate);

        return Response.builder()
                .responseMessage("Success")
                .rate(rateToUpdate)
                .build();
    }

    public double getRate(String currency){
        List<Rates> ratesList = rateRepository.findByStatus(Status.APPROVED);
        double rate = 0;
        if (currency.equalsIgnoreCase("USD")){
            rate = ratesList.stream()
                    .map(Rates::getUsdBid)
                    .filter(usdBid -> usdBid != 0)
                    .mapToDouble(Double::doubleValue)
                    .findFirst()
                    .orElse(0.0);
        }
        if (currency.equalsIgnoreCase("EUR")){
            rate = ratesList.stream()
                    .map(Rates::getEurBid)
                    .filter(eurBid -> eurBid != 0)
                    .mapToDouble(Double::doubleValue)
                    .findFirst()
                    .orElse(0.0);
        }
        if (currency.equalsIgnoreCase("GBP")){
            rate = ratesList.stream().map(Rates::getGbpBid)
                    .filter(gbpBid -> gbpBid != 0)
                    .mapToDouble(Double::doubleValue)
                    .findFirst()
                    .orElse(0.0);

        }

        return rate;

    }

}
