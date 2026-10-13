package com.payvantage.businessaccountonboarding.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpsertSignatoryRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "BVN must be 11 digits")
    private String bvn;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "NIN must be 11 digits")
    private String nin;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;
}
