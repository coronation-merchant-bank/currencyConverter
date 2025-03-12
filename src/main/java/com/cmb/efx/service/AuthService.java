package com.cmb.efx.service;

import com.cmb.efx.dto.*;
import com.cmb.efx.exception.AuthenticationException;
import com.cmb.efx.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;

    private final RestTemplate restTemplate;

    private final OtpService otpService;

    @Value("${cmb-staff-token}")
    private String token;

    @Value("${staff-role-baseUrl}")
    private String roleBaseUrl;

    @Value("${staff-role-token}")
    private String roleToken;

    @Value("${staff-login-baseUrl}")
    private String baseUrl;



    public Response ldapLogin(LdapLoginRequest request, String entityName) throws CustomException {
        String username = request.getUsername();
        System.out.println("User Attempting to user is:  " + request.getUsername());

        if (entityName != null && entityName.equalsIgnoreCase("coronation")) {
            // Step 1: Generate and send OTP if the request contains only a username (first login attempt)
            if (request.getOtpCode() == null || request.getOtpCode().isEmpty()) {
                otpService.generateOtp(OtpRequest.builder().identifier(username).build());
                return Response.builder()
                        .responseMessage("OTP sent to your registered email. Please enter the OTP to continue.")
                        .build();
            }

            // Step 2: Validate OTP
            if (request.getOtpCode() != null) {
                boolean isOtpValid = otpService.validateOtp(OtpValidationRequest.builder()
                        .identifier(username)
                        .otpCode(request.getOtpCode())
                        .build());
                if (!isOtpValid) {
                    throw new CustomException("Invalid OTP", HttpStatus.UNAUTHORIZED);
                }
            }



            // Step 3: Proceed with Password Authentication
            return authenticateUser(request, username);
        } else {
            // Step 3: Proceed with Password Authentication for other entities
            return authenticateUser(request, username);
        }
    }

    private Response authenticateUser(LdapLoginRequest request, String username) throws CustomException {
        String url = baseUrl + "?username=" + username
                + "&password=" + request.getPassword()
                + "&authorization=" + token;

        log.debug("Full URL: {}", url);
        HttpEntity<LdapLoginRequest> entity = new HttpEntity<>(request);

        try {
            ResponseEntity<AuthResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, AuthResponse.class);
            log.debug("Response: {}", responseEntity);

            if (responseEntity == null || responseEntity.getBody() == null) {
                throw new CustomException("Username or Password invalid.", HttpStatus.UNAUTHORIZED);
            }

            // Step 4: Retrieve user role and generate token
            RoleResponse roleResponse = getStaffRole(RoleRequest.builder().email(username).build());
            log.info("User Access Mngt Response: {}", roleResponse);

            String token = tokenService.generateToken(request);
            System.out.println("User Logged in at: " + LocalDateTime.now());
            return Response.builder()
                    .responseMessage("Success")
                    .staffRole(roleResponse)
                    .token(token)
                    .build();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw handleHttpException(ex);
        } catch (AuthenticationException e) {
            throw new CustomException("Username or Password invalid", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            throw new CustomException("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private CustomException handleHttpException(HttpStatusCodeException ex) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            int status = jsonNode.path("status").asInt();
            String description = jsonNode.path("description").asText();

            return new CustomException(description.isEmpty() ? "Username or Password invalid." : description, HttpStatus.valueOf(status));
        } catch (JsonProcessingException e) {
            return new CustomException("Error parsing error response.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public RoleResponse getStaffRole(RoleRequest request){

        RestTemplate restTemplate = new RestTemplate();
        String url = roleBaseUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.set("secret", roleToken);
        HttpEntity<RoleRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<RoleResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, RoleResponse.class);
        log.debug("response: {}", responseEntity);

        return responseEntity.getBody();
    }


}
