package com.cmb.efx.dto;


import com.cmb.efx.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateResponse {
    private double usdBid;
    private double gbpBid;
    private double eurBid;
    private double usdOffer;
    private double gbpOffer;
    private double eurOffer;
    private Status status;
    private String initiator;
    private String authorizer;
    private String createdAt;
    private String updatedAt;
    private String approveDate;
    private String rejectDate;
}
