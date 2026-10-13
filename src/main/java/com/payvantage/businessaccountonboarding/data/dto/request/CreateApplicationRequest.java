package com.payvantage.businessaccountonboarding.data.dto.request;

import com.payvantage.businessaccountonboarding.data.model.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateApplicationRequest {

    @NotNull
    private BusinessType businessType;

    /** Company Name / Business Name / Organisation Name, depending on businessType. */
    @NotBlank
    private String businessName;

    /** RC Number / BN Number / IT-CAC Number, depending on businessType. */
    @NotBlank
    private String registrationNumber;

    @NotBlank
    private String businessAddress;

    @NotBlank
    @Pattern(regexp = "\\d{9,14}", message = "TIN must be 9 to 14 digits")
    private String tin;
}
