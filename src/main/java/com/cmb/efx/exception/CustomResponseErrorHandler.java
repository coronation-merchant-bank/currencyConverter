package com.cmb.efx.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class CustomResponseErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        // Consider any HTTP status code 4xx or 5xx as an error
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // Parse the response body to extract error details
        com.cmb.efx.exception.ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        throw new CustomException(errorResponse.getDescription(), response.getStatusCode());
    }
}
