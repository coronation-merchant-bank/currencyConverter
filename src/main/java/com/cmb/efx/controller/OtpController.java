package com.cmb.efx.controller;

import com.cmb.efx.dto.OtpRequest;
import com.cmb.efx.dto.OtpValidationRequest;
import com.cmb.efx.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateOtp(@RequestBody @Valid OtpRequest request) {
        String otp = otpService.generateOtp(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP generated successfully");
        response.put("otp", otp); // In production, send via email/SMS instead
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validateOtp(@RequestBody @Valid OtpValidationRequest request) {
        boolean isValid = otpService.validateOtp(request);
        if (isValid) {
            return ResponseEntity.ok(Collections.singletonMap("message", "OTP is valid"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Invalid OTP"));
    }
}
