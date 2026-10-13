package com.payvantage.businessaccountonboarding.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpsertRefereeRequest {

    @NotBlank
    private String accountName;

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String bankName;

    @NotBlank
    private String email;

    @NotBlank
    private String phoneNumber;
}
