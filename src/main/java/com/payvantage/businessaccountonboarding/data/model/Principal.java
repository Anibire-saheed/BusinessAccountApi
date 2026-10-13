package com.payvantage.businessaccountonboarding.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An Admin Officer, Director, Proprietor or Trustee on an application, depending on role.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrincipalRole role;

    private String fullName;

    private String bvn;

    private String nin;

    private String phoneNumber;

    private String email;

    private String passportPhotoS3Key;

    private String validIdS3Key;

    private String signatureS3Key;

    @Column(nullable = false)
    private boolean alsoSignatory;

    /**
     * Set once {@link #alsoSignatory} is true: the id of the Signatory record kept in sync
     * with this principal's details. Null when alsoSignatory is false or has never been set.
     */
    private Long linkedSignatoryId;

    /**
     * BN Proprietor only: true when this principal's details are a synced copy of the
     * application's Admin Officer, per "Is the Proprietor the same as the Admin Officer?".
     */
    @Column(nullable = false)
    private boolean sameAsAdminOfficer;
}
