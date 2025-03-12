package com.cmb.efx.dto;

import com.cmb.efx.entity.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class VolumeLimitRequest {
    private double universalGlobalLimit;
    private String currencyType;
    private String startDate;
    private String endDate;
    private String initiator;
    private String authorizer;
    private Status status;
    private String approvedDate;
}
