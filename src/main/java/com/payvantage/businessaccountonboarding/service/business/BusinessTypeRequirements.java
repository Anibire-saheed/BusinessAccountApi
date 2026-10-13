package com.payvantage.businessaccountonboarding.service.business;

import com.payvantage.businessaccountonboarding.data.model.BusinessType;
import com.payvantage.businessaccountonboarding.data.model.DocumentType;
import com.payvantage.businessaccountonboarding.data.model.PrincipalRole;

import java.util.EnumSet;
import java.util.Set;

/**
 * Per-business-type document and principal requirements, per the
 * Ethica MFB Business Account Opening Portal BRD.
 */
public final class BusinessTypeRequirements {

    private BusinessTypeRequirements() {
    }

    public static Set<DocumentType> requiredDocuments(BusinessType type) {
        return switch (type) {
            case RC -> EnumSet.of(DocumentType.CAC_CERTIFICATE, DocumentType.CAC_STATUS_REPORT,
                    DocumentType.MEMART, DocumentType.BOARD_RESOLUTION);
            case BN -> EnumSet.of(DocumentType.CAC_CERTIFICATE, DocumentType.CAC_STATUS_REPORT);
            case IT -> EnumSet.of(DocumentType.CAC_CERTIFICATE, DocumentType.CAC_STATUS_REPORT,
                    DocumentType.BOARD_RESOLUTION);
        };
    }

    /** The role used for the repeatable principal section (Directors/Proprietor/Trustees). */
    public static PrincipalRole principalRole(BusinessType type) {
        return switch (type) {
            case RC -> PrincipalRole.DIRECTOR;
            case BN -> PrincipalRole.PROPRIETOR;
            case IT -> PrincipalRole.TRUSTEE;
        };
    }

    public static int minimumPrincipals(BusinessType type) {
        return switch (type) {
            case RC -> 2;
            case BN -> 1;
            case IT -> 2;
        };
    }
}
