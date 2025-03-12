package com.cmb.efx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversionResponse {
    private String convertedFrom;
    private String convertedTo;
    private String amountConverted;
    private double rate;
    private String conversionResult;
    private String offer;
}
