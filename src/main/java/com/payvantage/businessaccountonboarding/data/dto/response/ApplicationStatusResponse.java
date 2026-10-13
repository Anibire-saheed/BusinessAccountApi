package com.payvantage.businessaccountonboarding.data.dto.response;

import com.payvantage.businessaccountonboarding.data.model.ApplicationStatus;
import com.payvantage.businessaccountonboarding.data.model.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusResponse {

    private String applicationUUID;
    private BusinessType businessType;
    private ApplicationStatus status;
    private int completionPercent;
    private List<String> missingItems;
    private boolean treated;
}
