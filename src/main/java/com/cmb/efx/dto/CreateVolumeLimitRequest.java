package com.cmb.efx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateVolumeLimitRequest {
    private double universalGlobalLimit;
    private String currencyType;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String initiator;
}
