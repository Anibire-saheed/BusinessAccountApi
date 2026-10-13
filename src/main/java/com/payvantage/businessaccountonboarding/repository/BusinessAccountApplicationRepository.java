package com.payvantage.businessaccountonboarding.repository;

import com.payvantage.businessaccountonboarding.data.model.BusinessAccountApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessAccountApplicationRepository extends JpaRepository<BusinessAccountApplication, Long> {
    Optional<BusinessAccountApplication> findByApplicationUUID(String applicationUUID);

    List<BusinessAccountApplication> findByTreated(boolean treated);
}
