package com.cmb.efx.service;

import com.cmb.efx.dto.OtpRequest;
import com.cmb.efx.dto.OtpValidationRequest;
import com.cmb.efx.entity.Otp;
import com.cmb.efx.exception.CustomException;
import com.cmb.efx.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class OtpService {
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3; // Rate limiting

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;



    public String generateOtp(OtpRequest otpRequest) {
        Random random = new Random();
        String otp = String.valueOf(random.nextInt(900000) + 100000);

        System.out.println("User requesting otp is: " + otpRequest.getIdentifier());

        // Save OTP in DB (for tracking & audit purposes)
        Otp otpEntity = new Otp();
        otpEntity.setIdentifier(otpRequest.getIdentifier());
        otpEntity.setOtpCode(otp);
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpEntity.setOtpAttempts(0);
        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(otpRequest.getIdentifier(), otpRequest.getIdentifier(), otp);

        return otp;
    }

    public boolean validateOtp(OtpValidationRequest otpValidationRequest) {
        List<Otp> otpEntity = otpRepository.findByIdentifier(otpValidationRequest.getIdentifier());
        if (otpEntity.isEmpty()) {
            throw new CustomException("User hasn't received an OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Otp otp = otpEntity.get(otpEntity.size() - 1); // Get the most recent OTP
        log.info("Otp: {}", otp);

        if (otp.getOtpAttempts() >= MAX_ATTEMPTS) {
            throw new CustomException("Maximum OTP attempts exceeded", HttpStatus.UNAUTHORIZED);
        }

        if (!otp.getOtpCode().equals(otpValidationRequest.getOtpCode())) {
            otp.setOtpAttempts(otp.getOtpAttempts() + 1);
            otpRepository.save(otp); // Save the updated attempt count
            return false;
        }

        // OTP is valid; reset attempt count or perform other necessary actions
        otp.setOtpAttempts(0);
        otpRepository.save(otp); // Save the reset attempt count
        return true;
    }
}
