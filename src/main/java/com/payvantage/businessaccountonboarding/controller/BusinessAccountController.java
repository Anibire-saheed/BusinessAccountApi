package com.payvantage.businessaccountonboarding.controller;

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
import com.payvantage.businessaccountonboarding.service.business.BusinessAccountApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/business-account/applications")
public class BusinessAccountController {

    private final BusinessAccountApplicationService applicationService;

    public BusinessAccountController(BusinessAccountApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<?> createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        BaseResponse response = applicationService.createApplication(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllApplications(@RequestParam(required = false) Boolean treated) {
        List<ApplicationStatusResponse> responses = applicationService.getAllApplications(treated);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{applicationUUID}/business-details")
    public ResponseEntity<?> updateBusinessDetails(@PathVariable String applicationUUID,
                                                    @Valid @RequestBody UpdateBusinessDetailsRequest request) {
        BaseResponse response = applicationService.updateBusinessDetails(applicationUUID, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationUUID}/documents/{documentType}")
    public ResponseEntity<?> uploadDocument(@PathVariable String applicationUUID,
                                             @PathVariable DocumentType documentType,
                                             @RequestParam("file") MultipartFile file) {
        BaseResponse response = applicationService.uploadDocument(applicationUUID, documentType, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationUUID}/principals")
    public ResponseEntity<?> getPrincipals(@PathVariable String applicationUUID) {
        List<PrincipalResponse> response = applicationService.getPrincipals(applicationUUID);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationUUID}/principals")
    public ResponseEntity<?> addPrincipal(@PathVariable String applicationUUID,
                                           @Valid @RequestBody UpsertPrincipalRequest request) {
        BaseResponse response = applicationService.addOrUpdatePrincipal(applicationUUID, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{applicationUUID}/principals/{principalId}")
    public ResponseEntity<?> updatePrincipal(@PathVariable String applicationUUID,
                                              @PathVariable Long principalId,
                                              @Valid @RequestBody UpsertPrincipalRequest request) {
        BaseResponse response = applicationService.addOrUpdatePrincipal(applicationUUID, principalId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationUUID}/principals/{principalId}/files/{fileType}")
    public ResponseEntity<?> uploadPrincipalFile(@PathVariable String applicationUUID,
                                                  @PathVariable Long principalId,
                                                  @PathVariable PersonFileType fileType,
                                                  @RequestParam("file") MultipartFile file) {
        BaseResponse response = applicationService.uploadPrincipalFile(applicationUUID, principalId, fileType, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationUUID}/signatories")
    public ResponseEntity<?> getSignatories(@PathVariable String applicationUUID) {
        List<SignatoryResponse> response = applicationService.getSignatories(applicationUUID);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationUUID}/signatories")
    public ResponseEntity<?> addSignatory(@PathVariable String applicationUUID,
                                           @Valid @RequestBody UpsertSignatoryRequest request) {
        BaseResponse response = applicationService.addSignatory(applicationUUID, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{applicationUUID}/signatories/{signatoryId}")
    public ResponseEntity<?> updateSignatory(@PathVariable String applicationUUID,
                                              @PathVariable Long signatoryId,
                                              @Valid @RequestBody UpsertSignatoryRequest request) {
        BaseResponse response = applicationService.updateSignatory(applicationUUID, signatoryId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationUUID}/signatories/{signatoryId}/files/{fileType}")
    public ResponseEntity<?> uploadSignatoryFile(@PathVariable String applicationUUID,
                                                  @PathVariable Long signatoryId,
                                                  @PathVariable PersonFileType fileType,
                                                  @RequestParam("file") MultipartFile file) {
        BaseResponse response = applicationService.uploadSignatoryFile(applicationUUID, signatoryId, fileType, file);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{applicationUUID}/referee")
    public ResponseEntity<?> upsertReferee(@PathVariable String applicationUUID,
                                            @Valid @RequestBody UpsertRefereeRequest request) {
        BaseResponse response = applicationService.upsertReferee(applicationUUID, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationUUID}/status")
    public ResponseEntity<?> getStatus(@PathVariable String applicationUUID) {
        ApplicationStatusResponse response = applicationService.getStatus(applicationUUID);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{applicationUUID}/confirmation")
    public ResponseEntity<?> confirmAccuracy(@PathVariable String applicationUUID,
                                              @RequestParam boolean confirmed) {
        BaseResponse response = applicationService.confirmAccuracy(applicationUUID, confirmed);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationUUID}/submit")
    public ResponseEntity<?> submit(@PathVariable String applicationUUID) {
        BaseResponse response = applicationService.submit(applicationUUID);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{applicationUUID}/mark-as-treated")
    public ResponseEntity<?> markAsTreated(@PathVariable String applicationUUID) {
        BaseResponse response = applicationService.markAsTreated(applicationUUID);
        return ResponseEntity.ok(response);
    }
}
