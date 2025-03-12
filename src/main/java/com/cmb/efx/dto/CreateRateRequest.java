package com.cmb.efx.dto;

import com.cmb.efx.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRateRequest {
    private double usdBid;
    private double gbpBid;
    private double eurBid;
    private double usdOffer;
    private double gbpOffer;
    private double eurOffer;
    private Status status;
    private String initiator;
}
