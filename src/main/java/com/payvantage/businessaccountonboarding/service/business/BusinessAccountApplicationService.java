package com.payvantage.businessaccountonboarding.service.business;

import com.payvantage.businessaccountonboarding.data.dto.request.CreateApplicationRequest;
import com.payvantage.businessaccountonboarding.data.dto.request.UpdateBusinessDetailsRequest;
import com.payvantage.businessaccountonboarding.data.dto.request.UpsertPrincipalRequest;
import com.payvantage.businessaccountonboarding.data.dto.request.UpsertRefereeRequest;
import com.payvantage.businessaccountonboarding.data.dto.request.UpsertSignatoryRequest;
import com.payvantage.businessaccountonboarding.data.dto.response.ApplicationStatusResponse;
import com.payvantage.businessaccountonboarding.data.dto.response.BaseResponse;
import com.payvantage.businessaccountonboarding.data.dto.response.PrincipalResponse;
import com.payvantage.businessaccountonboarding.data.dto.response.SignatoryResponse;
import com.payvantage.businessaccountonboarding.data.model.DocumentType;
import com.payvantage.businessaccountonboarding.data.model.PersonFileType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BusinessAccountApplicationService {

    BaseResponse createApplication(CreateApplicationRequest request);

    List<ApplicationStatusResponse> getAllApplications(Boolean treated);

    BaseResponse updateBusinessDetails(String applicationUUID, UpdateBusinessDetailsRequest request);

    BaseResponse uploadDocument(String applicationUUID, DocumentType documentType, MultipartFile file);

    BaseResponse addOrUpdatePrincipal(String applicationUUID, UpsertPrincipalRequest request);

    BaseResponse addOrUpdatePrincipal(String applicationUUID, Long principalId, UpsertPrincipalRequest request);

    List<PrincipalResponse> getPrincipals(String applicationUUID);

    BaseResponse uploadPrincipalFile(String applicationUUID, Long principalId, PersonFileType fileType, MultipartFile file);

    BaseResponse addSignatory(String applicationUUID, UpsertSignatoryRequest request);

    BaseResponse updateSignatory(String applicationUUID, Long signatoryId, UpsertSignatoryRequest request);

    List<SignatoryResponse> getSignatories(String applicationUUID);

    BaseResponse uploadSignatoryFile(String applicationUUID, Long signatoryId, PersonFileType fileType, MultipartFile file);

    BaseResponse upsertReferee(String applicationUUID, UpsertRefereeRequest request);

    ApplicationStatusResponse getStatus(String applicationUUID);

    BaseResponse confirmAccuracy(String applicationUUID, boolean confirmed);

    BaseResponse submit(String applicationUUID);

    BaseResponse markAsTreated(String applicationUUID);
}
