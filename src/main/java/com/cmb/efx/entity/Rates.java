package com.cmb.efx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Rates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double usdBid;
    private double gbpBid;
    private double eurBid;
    private double usdOffer;
    private double gbpOffer;
    private double eurOffer;
    @Enumerated(value = EnumType.STRING)
    @Column(length = 20)
    private Status status;
    private String initiator;
    private String authorizer;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime approveDate;
    private LocalDateTime rejectDate;
}
