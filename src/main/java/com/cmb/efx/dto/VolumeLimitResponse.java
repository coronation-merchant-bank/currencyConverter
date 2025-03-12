package com.cmb.efx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VolumeLimitResponse {
    private Long id;
    private String sessionId;
    private String status;
    private String initiator;
    private String dateInitiated;
    private String authorizer;
    private String currencyType;
    private double globalLimit;
    private double used;
    private double unused;
    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;
    private String approvedDate;
}
