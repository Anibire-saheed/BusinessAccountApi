package com.payvantage.businessaccountonboarding.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Signatory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    private String fullName;

    private String bvn;

    private String nin;

    private String phoneNumber;

    private String email;

    private String passportPhotoS3Key;

    private String validIdS3Key;

    private String signatureS3Key;

    /**
     * Set when this signatory is a synced copy of a Director/Proprietor/Trustee who
     * answered "Yes" to also being an Account Signatory. Null when added directly
     * via "+ Add Signatory".
     */
    private Long sourcePrincipalId;
}
