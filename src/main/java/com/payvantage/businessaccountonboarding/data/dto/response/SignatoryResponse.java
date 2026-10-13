package com.payvantage.businessaccountonboarding.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignatoryResponse {

    private Long id;
    private String fullName;
    private String bvn;
    private String nin;
    private String phoneNumber;
    private String email;
    private boolean passportPhotoUploaded;
    private boolean validIdUploaded;
    private boolean signatureUploaded;
    private boolean addedAutomatically;
}
