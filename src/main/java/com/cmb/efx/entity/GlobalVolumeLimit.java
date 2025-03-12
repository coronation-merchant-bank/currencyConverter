package com.cmb.efx.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class GlobalVolumeLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double universalGlobalLimit;
    private double usdLimit;
    private double eurLimit;
    private double gbpLimit;
    @Column(nullable = true)
    private double usedVolume;
    private String currencyType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime modifiedAt;
    private String initiator;
    private String authorizer;
    private LocalDateTime approvedDate;
    private LocalDateTime rejectDate;
    @Enumerated(value = EnumType.STRING)
    @Column(length = 255)
    private Status status;
    private String usedFlag;
}
