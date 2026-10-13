package com.payvantage.businessaccountonboarding.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateBusinessDetailsRequest {

    @NotBlank
    private String businessName;

    @NotBlank
    private String registrationNumber;

    @NotBlank
    private String businessAddress;

    @NotBlank
    @Pattern(regexp = "\\d{9,14}", message = "TIN must be 9 to 14 digits")
    private String tin;
}
