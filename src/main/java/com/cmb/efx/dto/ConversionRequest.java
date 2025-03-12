package com.cmb.efx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversionRequest {
    private String fromCurrency;
    private String toCurrency;
    private double rate;
    private String amountToConvert;
    private String fromAccount;
    private String toAccount;
}
