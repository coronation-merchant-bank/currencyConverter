package com.cmb.efx.util;


import com.cmb.efx.dto.CurrencyRequest;
import com.cmb.efx.dto.RateResponse;
import com.cmb.efx.dto.VolumeLimitResponse;
import com.cmb.efx.entity.Currency;
import com.cmb.efx.entity.GlobalVolumeLimit;
import com.cmb.efx.entity.Rates;

import java.time.LocalDateTime;

public class Mapper {

    public static CurrencyRequest mapCurrencyToDto(Currency currency){
        return CurrencyRequest.builder()
                .currencyName(currency.getCurrencyName())
                .currencySymbol(currency.getCurrencySymbol())
                .country(currency.getCountry())
                .build();
    }

    public static RateResponse mapRateToDto(Rates rates){
        return RateResponse.builder()
                .usdBid(rates.getUsdBid())
                .gbpBid(rates.getGbpBid())
                .eurBid(rates.getEurBid())
                .usdOffer(rates.getUsdOffer())
                .gbpOffer(rates.getGbpOffer())
                .eurOffer(rates.getEurOffer())
                .status(rates.getStatus())
                .initiator(rates.getInitiator())
                .authorizer(rates.getAuthorizer())
                .createdAt(rates.getCreatedAt().toString())
                .updatedAt(rates.getUpdatedAt().toString())
                .approveDate(rates.getApproveDate().toString())
                .rejectDate(rates.getRejectDate().toString())
                .build();
    }

    public static VolumeLimitResponse mapGlvToVolumeLimitResponse(GlobalVolumeLimit limit){
        LocalDateTime approvedDate = LocalDateTime.now();
        if (limit.getApprovedDate() != null){
            approvedDate = limit.getApprovedDate();
        }
        return VolumeLimitResponse.builder()
                .id(limit.getId())
                .sessionId(limit.getInitiator())
                .status(limit.getStatus().toString())
                .initiator(limit.getInitiator())
                .dateInitiated(limit.getCreatedAt().toString())
                .authorizer(limit.getAuthorizer())
                .currencyType(limit.getCurrencyType())
                .globalLimit(limit.getUniversalGlobalLimit())
                .startTime(limit.getStartDate().toLocalTime().toString())
                .endTime(limit.getEndDate().toLocalTime().toString())
                .startDate(limit.getStartDate().toLocalDate().toString())
                .endDate(limit.getEndDate().toLocalDate().toString())
                .used(limit.getUsedVolume())
                .unused(limit.getUniversalGlobalLimit() - limit.getUsedVolume())
                .approvedDate(approvedDate.toString())
                .build();
    }


}
