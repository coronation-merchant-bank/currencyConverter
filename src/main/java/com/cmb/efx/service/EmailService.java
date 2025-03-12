package com.cmb.efx.service;

import com.cmb.efx.dto.Response;
import com.cmb.efx.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${email.service.url}")
    private String emailServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtpEmail(String recipientName, String emailAddress, String otp) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("from", "noreply-bulkservice@coronationmb.com");
        requestBody.put("to", emailAddress);
        requestBody.put("subject", "Action Required: OTP for your access to eFX Naira Conversion Portal");
        requestBody.put("html", String.format(
                "Dear %s,\n" +
                        "To proceed with your login to the eFX Naira Conversion portal, please use the OTP below." +
                        "Your OTP is: <b>%s</b>.<br/><br/>" +
                        "If you did not request this otp, please contact our support team immediately.<br/><br/>" +
                        "Thank you for using our services.<br/>Coronation Merchant Bank",
                recipientName, otp
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(emailServiceUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("Failed to send OTP email: " + response.getBody(), response.getStatusCode());
        }
    }

}
