package com.payvantage.businessaccountonboarding.repository;

import com.payvantage.businessaccountonboarding.data.model.Signatory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignatoryRepository extends JpaRepository<Signatory, Long> {
    List<Signatory> findByApplicationId(Long applicationId);

    Optional<Signatory> findBySourcePrincipalId(Long sourcePrincipalId);
}
