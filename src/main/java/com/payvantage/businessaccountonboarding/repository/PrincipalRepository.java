package com.payvantage.businessaccountonboarding.repository;

import com.payvantage.businessaccountonboarding.data.model.Principal;
import com.payvantage.businessaccountonboarding.data.model.PrincipalRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrincipalRepository extends JpaRepository<Principal, Long> {
    List<Principal> findByApplicationId(Long applicationId);

    List<Principal> findByApplicationIdAndRole(Long applicationId, PrincipalRole role);

    Optional<Principal> findByApplicationIdAndRoleAndSameAsAdminOfficerTrue(Long applicationId, PrincipalRole role);
}
