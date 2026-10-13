package com.payvantage.businessaccountonboarding.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class BusinessAccountApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String applicationUUID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessType businessType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    /** Company Name / Business Name / Organisation Name, depending on businessType. */
    private String businessName;

    /** RC Number / BN Number / IT-CAC Number, depending on businessType. */
    private String registrationNumber;

    private String businessAddress;

    private String tin;

    @Column(nullable = false)
    private boolean confirmedAccurate;

    @Column(nullable = false)
    private boolean treated;

    private LocalDateTime treatedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
