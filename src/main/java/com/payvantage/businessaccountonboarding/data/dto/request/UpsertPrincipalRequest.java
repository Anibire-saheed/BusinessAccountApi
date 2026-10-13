package com.payvantage.businessaccountonboarding.data.dto.request;

import com.payvantage.businessaccountonboarding.data.model.PrincipalRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpsertPrincipalRequest {

    @NotNull
    private PrincipalRole role;

    /**
     * BN Proprietor only: when true, fullName/bvn/nin/phoneNumber/email are ignored and the
     * principal's details are instead copied from the application's Admin Officer.
     */
    private boolean sameAsAdminOfficer;

    private String fullName;

    @Pattern(regexp = "\\d{11}", message = "BVN must be 11 digits")
    private String bvn;

    @Pattern(regexp = "\\d{11}", message = "NIN must be 11 digits")
    private String nin;

    private String phoneNumber;

    @Email
    private String email;

    /** Is this Director/Proprietor/Trustee also an Account Signatory? */
    private boolean alsoSignatory;
}
