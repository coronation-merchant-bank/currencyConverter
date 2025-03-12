package com.cmb.efx.dto;

import com.cmb.efx.entity.GlobalVolumeLimit;
import com.cmb.efx.entity.Rates;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private String responseMessage;
    private CurrencyRequest currency;
    private List<CurrencyRequest> currencyList;
    private ConversionResponse conversionResponse;
    private CreateRateRequest rates;
    private Rates rate;
    private List<Rates> ratesList;
    private RateResponse rateResponse;
    private List<RateResponse> rateResponseList;
    private List<VolumeLimitResponse> volumeLimitList;
    private AuthResponse authResponse;
    private RoleResponse staffRole;
    private String token;
    private TransferResponse transferResponse;
    private AccountEnquiry accountDetails;
    private Integer totalElements;
    private GlobalVolumeLimit volumeLimit;
    private Map<String, Double> globalVolumeLimitMap;
}
