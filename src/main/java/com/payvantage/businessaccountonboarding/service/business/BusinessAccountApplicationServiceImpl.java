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
import com.payvantage.businessaccountonboarding.data.model.*;
import com.payvantage.businessaccountonboarding.exception.ApplicationNotFoundException;
import com.payvantage.businessaccountonboarding.exception.InvalidApplicationStateException;
import com.payvantage.businessaccountonboarding.exception.PrincipalNotFoundException;
import com.payvantage.businessaccountonboarding.exception.SignatoryNotFoundException;
import com.payvantage.businessaccountonboarding.repository.BusinessAccountApplicationRepository;
import com.payvantage.businessaccountonboarding.repository.BusinessDocumentRepository;
import com.payvantage.businessaccountonboarding.repository.BusinessRefereeRepository;
import com.payvantage.businessaccountonboarding.repository.PrincipalRepository;
import com.payvantage.businessaccountonboarding.repository.SignatoryRepository;
import com.payvantage.businessaccountonboarding.service.storage.DocumentStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class BusinessAccountApplicationServiceImpl implements BusinessAccountApplicationService {

    private final BusinessAccountApplicationRepository applicationRepository;
    private final BusinessDocumentRepository documentRepository;
    private final PrincipalRepository principalRepository;
    private final SignatoryRepository signatoryRepository;
    private final BusinessRefereeRepository refereeRepository;
    private final DocumentStorageService documentStorageService;

    public BusinessAccountApplicationServiceImpl(
            BusinessAccountApplicationRepository applicationRepository,
            BusinessDocumentRepository documentRepository,
            PrincipalRepository principalRepository,
            SignatoryRepository signatoryRepository,
            BusinessRefereeRepository refereeRepository,
            DocumentStorageService documentStorageService) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.principalRepository = principalRepository;
        this.signatoryRepository = signatoryRepository;
        this.refereeRepository = refereeRepository;
        this.documentStorageService = documentStorageService;
    }

    @Override
    public BaseResponse createApplication(CreateApplicationRequest request) {
        BusinessAccountApplication application = new BusinessAccountApplication();
        application.setApplicationUUID(UUID.randomUUID().toString());
        application.setBusinessType(request.getBusinessType());
        application.setBusinessName(request.getBusinessName());
        application.setRegistrationNumber(request.getRegistrationNumber());
        application.setBusinessAddress(request.getBusinessAddress());
        application.setTin(request.getTin());
        application.setStatus(ApplicationStatus.DRAFT);
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);

        return ok("Application created", application.getApplicationUUID());
    }

    @Override
    @Transactional
    public BaseResponse updateBusinessDetails(String applicationUUID, UpdateBusinessDetailsRequest request) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        application.setBusinessName(request.getBusinessName());
        application.setRegistrationNumber(request.getRegistrationNumber());
        application.setBusinessAddress(request.getBusinessAddress());
        application.setTin(request.getTin());
        touch(application);
        return ok("Business details updated", null);
    }

    @Override
    @Transactional
    public BaseResponse uploadDocument(String applicationUUID, DocumentType documentType, MultipartFile file) {
        BusinessAccountApplication application = findApplication(applicationUUID);

        Set<DocumentType> required = BusinessTypeRequirements.requiredDocuments(application.getBusinessType());
        if (!required.contains(documentType)) {
            return error("Document type " + documentType + " is not required for " + application.getBusinessType());
        }

        String s3Key = documentStorageService.upload("business-applications/" + applicationUUID + "/documents", file);

        BusinessDocument document = documentRepository
                .findByApplicationIdAndDocumentType(application.getId(), documentType)
                .orElseGet(BusinessDocument::new);
        document.setApplicationId(application.getId());
        document.setDocumentType(documentType);
        document.setS3Key(s3Key);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setUploadedAt(LocalDateTime.now());
        documentRepository.save(document);

        touch(application);
        return ok("Document uploaded", s3Key);
    }

    @Override
    @Transactional
    public BaseResponse addOrUpdatePrincipal(String applicationUUID, UpsertPrincipalRequest request) {
        return addOrUpdatePrincipal(applicationUUID, null, request);
    }

    @Override
    @Transactional
    public BaseResponse addOrUpdatePrincipal(String applicationUUID, Long principalId, UpsertPrincipalRequest request) {
        BusinessAccountApplication application = findApplication(applicationUUID);

        Principal principal;
        if (principalId != null) {
            principal = principalRepository.findById(principalId)
                    .filter(p -> p.getApplicationId().equals(application.getId()))
                    .orElseThrow(() -> new PrincipalNotFoundException(principalId));
        } else {
            principal = new Principal();
            principal.setApplicationId(application.getId());
        }

        principal.setRole(request.getRole());

        if (request.getRole() == PrincipalRole.PROPRIETOR && request.isSameAsAdminOfficer()) {
            copyAdminOfficerDetailsOnto(principal, application);
        } else {
            principal.setSameAsAdminOfficer(false);
            if (isBlank(request.getFullName())) {
                return error("fullName is required");
            }
            principal.setFullName(request.getFullName());
            principal.setBvn(request.getBvn());
            principal.setNin(request.getNin());
            principal.setPhoneNumber(request.getPhoneNumber());
            principal.setEmail(request.getEmail());
        }

        principal.setAlsoSignatory(request.isAlsoSignatory());
        principalRepository.save(principal);

        syncLinkedSignatory(principal);

        if (request.getRole() == PrincipalRole.ADMIN_OFFICER) {
            resyncProprietorsCopiedFromAdminOfficer(application, principal);
        }

        touch(application);
        return ok("Principal saved", principal.getId());
    }

    private void copyAdminOfficerDetailsOnto(Principal proprietor, BusinessAccountApplication application) {
        Principal adminOfficer = principalRepository.findByApplicationIdAndRole(application.getId(), PrincipalRole.ADMIN_OFFICER)
                .stream().findFirst()
                .orElseThrow(() -> new InvalidApplicationStateException("Admin Officer must be added before the Proprietor can be marked as the same person"));

        proprietor.setSameAsAdminOfficer(true);
        proprietor.setFullName(adminOfficer.getFullName());
        proprietor.setBvn(adminOfficer.getBvn());
        proprietor.setNin(adminOfficer.getNin());
        proprietor.setPhoneNumber(adminOfficer.getPhoneNumber());
        proprietor.setEmail(adminOfficer.getEmail());
        proprietor.setPassportPhotoS3Key(adminOfficer.getPassportPhotoS3Key());
        proprietor.setValidIdS3Key(adminOfficer.getValidIdS3Key());
        proprietor.setSignatureS3Key(adminOfficer.getSignatureS3Key());
    }

    /** Keeps any Proprietor previously marked "same as Admin Officer" in sync when the Admin Officer changes. */
    private void resyncProprietorsCopiedFromAdminOfficer(BusinessAccountApplication application, Principal adminOfficer) {
        List<Principal> proprietors = principalRepository.findByApplicationIdAndRole(application.getId(), PrincipalRole.PROPRIETOR);
        for (Principal proprietor : proprietors) {
            if (proprietor.isSameAsAdminOfficer()) {
                copyAdminOfficerDetailsOnto(proprietor, application);
                proprietor.setAlsoSignatory(adminOfficer.isAlsoSignatory());
                principalRepository.save(proprietor);
                syncLinkedSignatory(proprietor);
            }
        }
    }

    /** Creates, updates, or removes the Signatory record linked to a principal per its alsoSignatory flag. */
    private void syncLinkedSignatory(Principal principal) {
        if (principal.isAlsoSignatory()) {
            Signatory signatory = signatoryRepository.findBySourcePrincipalId(principal.getId())
                    .orElseGet(Signatory::new);
            signatory.setApplicationId(principal.getApplicationId());
            signatory.setSourcePrincipalId(principal.getId());
            signatory.setFullName(principal.getFullName());
            signatory.setBvn(principal.getBvn());
            signatory.setNin(principal.getNin());
            signatory.setPhoneNumber(principal.getPhoneNumber());
            signatory.setEmail(principal.getEmail());
            signatory.setPassportPhotoS3Key(principal.getPassportPhotoS3Key());
            signatory.setValidIdS3Key(principal.getValidIdS3Key());
            signatory.setSignatureS3Key(principal.getSignatureS3Key());
            signatoryRepository.save(signatory);
            principal.setLinkedSignatoryId(signatory.getId());
            principalRepository.save(principal);
        } else {
            signatoryRepository.findBySourcePrincipalId(principal.getId())
                    .ifPresent(signatory -> {
                        signatoryRepository.delete(signatory);
                        principal.setLinkedSignatoryId(null);
                        principalRepository.save(principal);
                    });
        }
    }

    @Override
    public List<PrincipalResponse> getPrincipals(String applicationUUID) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        return principalRepository.findByApplicationId(application.getId()).stream()
                .map(this::toPrincipalResponse)
                .toList();
    }

    private PrincipalResponse toPrincipalResponse(Principal principal) {
        return new PrincipalResponse(
                principal.getId(),
                principal.getRole(),
                principal.getFullName(),
                principal.getBvn(),
                principal.getNin(),
                principal.getPhoneNumber(),
                principal.getEmail(),
                principal.getPassportPhotoS3Key() != null,
                principal.getValidIdS3Key() != null,
                principal.getSignatureS3Key() != null,
                principal.isAlsoSignatory(),
                principal.isSameAsAdminOfficer());
    }

    @Override
    @Transactional
    public BaseResponse uploadPrincipalFile(String applicationUUID, Long principalId, PersonFileType fileType, MultipartFile file) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        Principal principal = principalRepository.findById(principalId)
                .filter(p -> p.getApplicationId().equals(application.getId()))
                .orElseThrow(() -> new PrincipalNotFoundException(principalId));

        if (principal.isSameAsAdminOfficer()) {
            return error("This principal's files are copied from the Admin Officer; upload to the Admin Officer instead");
        }

        String s3Key = documentStorageService.upload(
                "business-applications/" + applicationUUID + "/principals/" + principalId, file);
        applyPersonFile(fileType, s3Key, principal::setPassportPhotoS3Key, principal::setValidIdS3Key, principal::setSignatureS3Key);
        principalRepository.save(principal);

        syncLinkedSignatory(principal);
        if (principal.getRole() == PrincipalRole.ADMIN_OFFICER) {
            resyncProprietorsCopiedFromAdminOfficer(application, principal);
        }

        touch(application);
        return ok("File uploaded", s3Key);
    }

    @Override
    @Transactional
    public BaseResponse addSignatory(String applicationUUID, UpsertSignatoryRequest request) {
        BusinessAccountApplication application = findApplication(applicationUUID);

        Signatory signatory = new Signatory();
        signatory.setApplicationId(application.getId());
        signatory.setFullName(request.getFullName());
        signatory.setBvn(request.getBvn());
        signatory.setNin(request.getNin());
        signatory.setPhoneNumber(request.getPhoneNumber());
        signatory.setEmail(request.getEmail());
        signatoryRepository.save(signatory);

        touch(application);
        return ok("Signatory added", signatory.getId());
    }

    @Override
    @Transactional
    public BaseResponse updateSignatory(String applicationUUID, Long signatoryId, UpsertSignatoryRequest request) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        Signatory signatory = signatoryRepository.findById(signatoryId)
                .filter(s -> s.getApplicationId().equals(application.getId()))
                .orElseThrow(() -> new SignatoryNotFoundException(signatoryId));

        if (signatory.getSourcePrincipalId() != null) {
            return error("This signatory was added automatically; edit the linked principal instead");
        }

        signatory.setFullName(request.getFullName());
        signatory.setBvn(request.getBvn());
        signatory.setNin(request.getNin());
        signatory.setPhoneNumber(request.getPhoneNumber());
        signatory.setEmail(request.getEmail());
        signatoryRepository.save(signatory);

        touch(application);
        return ok("Signatory updated", signatory.getId());
    }

    @Override
    public List<SignatoryResponse> getSignatories(String applicationUUID) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        return signatoryRepository.findByApplicationId(application.getId()).stream()
                .map(this::toSignatoryResponse)
                .toList();
    }

    private SignatoryResponse toSignatoryResponse(Signatory signatory) {
        return new SignatoryResponse(
                signatory.getId(),
                signatory.getFullName(),
                signatory.getBvn(),
                signatory.getNin(),
                signatory.getPhoneNumber(),
                signatory.getEmail(),
                signatory.getPassportPhotoS3Key() != null,
                signatory.getValidIdS3Key() != null,
                signatory.getSignatureS3Key() != null,
                signatory.getSourcePrincipalId() != null);
    }

    @Override
    @Transactional
    public BaseResponse uploadSignatoryFile(String applicationUUID, Long signatoryId, PersonFileType fileType, MultipartFile file) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        Signatory signatory = signatoryRepository.findById(signatoryId)
                .filter(s -> s.getApplicationId().equals(application.getId()))
                .orElseThrow(() -> new SignatoryNotFoundException(signatoryId));

        if (signatory.getSourcePrincipalId() != null) {
            return error("This signatory's files are copied from the linked principal; upload there instead");
        }

        String s3Key = documentStorageService.upload(
                "business-applications/" + applicationUUID + "/signatories/" + signatoryId, file);
        applyPersonFile(fileType, s3Key, signatory::setPassportPhotoS3Key, signatory::setValidIdS3Key, signatory::setSignatureS3Key);
        signatoryRepository.save(signatory);

        touch(application);
        return ok("File uploaded", s3Key);
    }

    private void applyPersonFile(PersonFileType fileType, String s3Key,
                                  java.util.function.Consumer<String> setPassportPhoto,
                                  java.util.function.Consumer<String> setValidId,
                                  java.util.function.Consumer<String> setSignature) {
        switch (fileType) {
            case PASSPORT_PHOTO -> setPassportPhoto.accept(s3Key);
            case VALID_ID -> setValidId.accept(s3Key);
            case E_SIGNATURE -> setSignature.accept(s3Key);
        }
    }

    @Override
    @Transactional
    public BaseResponse upsertReferee(String applicationUUID, UpsertRefereeRequest request) {
        BusinessAccountApplication application = findApplication(applicationUUID);

        BusinessReferee referee = refereeRepository.findByApplicationId(application.getId())
                .orElseGet(BusinessReferee::new);
        referee.setApplicationId(application.getId());
        referee.setAccountName(request.getAccountName());
        referee.setAccountNumber(request.getAccountNumber());
        referee.setBankName(request.getBankName());
        referee.setEmail(request.getEmail());
        referee.setPhoneNumber(request.getPhoneNumber());
        refereeRepository.save(referee);

        touch(application);
        return ok("Referee saved", referee.getId());
    }

    @Override
    public ApplicationStatusResponse getStatus(String applicationUUID) {
        return toStatusResponse(findApplication(applicationUUID));
    }

    @Override
    public List<ApplicationStatusResponse> getAllApplications(Boolean treated) {
        List<BusinessAccountApplication> applications = treated == null
                ? applicationRepository.findAll()
                : applicationRepository.findByTreated(treated);

        return applications.stream()
                .map(this::toStatusResponse)
                .toList();
    }

    private ApplicationStatusResponse toStatusResponse(BusinessAccountApplication application) {
        List<ChecklistItem> checklist = buildChecklist(application);

        int totalChecks = checklist.size();
        long completed = checklist.stream().filter(ChecklistItem::satisfied).count();
        int percent = totalChecks == 0 ? 0 : (int) (completed * 100 / totalChecks);

        return new ApplicationStatusResponse(
                application.getApplicationUUID(),
                application.getBusinessType(),
                application.getStatus(),
                percent,
                missingNames(checklist),
                application.isTreated());
    }

    @Override
    @Transactional
    public BaseResponse confirmAccuracy(String applicationUUID, boolean confirmed) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        application.setConfirmedAccurate(confirmed);
        touch(application);
        return ok("Confirmation recorded", null);
    }

    @Override
    @Transactional
    public BaseResponse submit(String applicationUUID) {
        BusinessAccountApplication application = findApplication(applicationUUID);

        if (!application.isConfirmedAccurate()) {
            return error("You must confirm that the information and documents provided are accurate before submitting");
        }

        List<String> missing = missingNames(buildChecklist(application));
        if (!missing.isEmpty()) {
            return error("Application is incomplete: " + String.join(", ", missing));
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        touch(application);
        return ok("Application submitted", application.getApplicationUUID());
    }

    @Override
    @Transactional
    public BaseResponse markAsTreated(String applicationUUID) {
        BusinessAccountApplication application = findApplication(applicationUUID);
        application.setTreated(true);
        application.setTreatedAt(LocalDateTime.now());
        touch(application);
        return ok("Application marked as treated", application.getApplicationUUID());
    }


    private record ChecklistItem(String name, boolean satisfied) {
    }

    private List<ChecklistItem> buildChecklist(BusinessAccountApplication application) {
        List<ChecklistItem> checklist = new ArrayList<>();

        checklist.add(new ChecklistItem("BUSINESS_NAME", !isBlank(application.getBusinessName())));
        checklist.add(new ChecklistItem("REGISTRATION_NUMBER", !isBlank(application.getRegistrationNumber())));
        checklist.add(new ChecklistItem("BUSINESS_ADDRESS", !isBlank(application.getBusinessAddress())));
        checklist.add(new ChecklistItem("TIN", !isBlank(application.getTin())));

        Set<DocumentType> required = BusinessTypeRequirements.requiredDocuments(application.getBusinessType());
        List<BusinessDocument> documents = documentRepository.findByApplicationId(application.getId());
        for (DocumentType type : required) {
            boolean present = documents.stream().anyMatch(d -> d.getDocumentType() == type);
            checklist.add(new ChecklistItem(type.name(), present));
        }

        List<Principal> principals = principalRepository.findByApplicationId(application.getId());

        boolean hasAdminOfficer = principals.stream()
                .anyMatch(p -> p.getRole() == PrincipalRole.ADMIN_OFFICER && isPersonComplete(p));
        checklist.add(new ChecklistItem("ADMIN_OFFICER", hasAdminOfficer));

        PrincipalRole principalRole = BusinessTypeRequirements.principalRole(application.getBusinessType());
        long principalCount = principals.stream()
                .filter(p -> p.getRole() == principalRole && isPersonComplete(p))
                .count();
        int minimumPrincipals = BusinessTypeRequirements.minimumPrincipals(application.getBusinessType());
        checklist.add(new ChecklistItem(principalRole.name() + "_COUNT", principalCount >= minimumPrincipals));

        long signatoryCount = signatoryRepository.findByApplicationId(application.getId()).stream()
                .filter(this::isPersonComplete)
                .count();
        checklist.add(new ChecklistItem("SIGNATORY_COUNT", signatoryCount >= 1));

        Optional<BusinessReferee> referee = refereeRepository.findByApplicationId(application.getId());
        checklist.add(new ChecklistItem("REFEREE", referee.isPresent()));

        return checklist;
    }

    private boolean isPersonComplete(Principal p) {
        return !isBlank(p.getFullName()) && !isBlank(p.getBvn()) && !isBlank(p.getNin())
                && !isBlank(p.getPhoneNumber()) && !isBlank(p.getEmail())
                && p.getPassportPhotoS3Key() != null && p.getValidIdS3Key() != null && p.getSignatureS3Key() != null;
    }

    private boolean isPersonComplete(Signatory s) {
        return !isBlank(s.getFullName()) && !isBlank(s.getBvn()) && !isBlank(s.getNin())
                && !isBlank(s.getPhoneNumber()) && !isBlank(s.getEmail())
                && s.getPassportPhotoS3Key() != null && s.getValidIdS3Key() != null && s.getSignatureS3Key() != null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<String> missingNames(List<ChecklistItem> checklist) {
        return checklist.stream()
                .filter(item -> !item.satisfied())
                .map(ChecklistItem::name)
                .toList();
    }

    private BusinessAccountApplication findApplication(String applicationUUID) {
        return applicationRepository.findByApplicationUUID(applicationUUID)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationUUID));
    }

    private void touch(BusinessAccountApplication application) {
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }

    private BaseResponse ok(String description, Object data) {
        BaseResponse response = new BaseResponse();
        response.setStatusCode(200);
        response.setDescription(description);
        response.setData(data);
        return response;
    }

    private BaseResponse error(String description) {
        BaseResponse response = new BaseResponse();
        response.setStatusCode(400);
        response.setDescription(description);
        return response;
    }
}
