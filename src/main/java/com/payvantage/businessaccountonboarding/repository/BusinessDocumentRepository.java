package com.payvantage.businessaccountonboarding.repository;

import com.payvantage.businessaccountonboarding.data.model.BusinessDocument;
import com.payvantage.businessaccountonboarding.data.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessDocumentRepository extends JpaRepository<BusinessDocument, Long> {
    List<BusinessDocument> findByApplicationId(Long applicationId);

    Optional<BusinessDocument> findByApplicationIdAndDocumentType(Long applicationId, DocumentType documentType);
}
