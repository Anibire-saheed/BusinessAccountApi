package com.payvantage.businessaccountonboarding.data.dto.response;

import com.payvantage.businessaccountonboarding.data.model.PrincipalRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrincipalResponse {

    private Long id;
    private PrincipalRole role;
    private String fullName;
    private String bvn;
    private String nin;
    private String phoneNumber;
    private String email;
    private boolean passportPhotoUploaded;
    private boolean validIdUploaded;
    private boolean signatureUploaded;
    private boolean alsoSignatory;
    private boolean sameAsAdminOfficer;
}
