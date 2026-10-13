package com.payvantage.businessaccountonboarding.repository;

import com.payvantage.businessaccountonboarding.data.model.BusinessReferee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRefereeRepository extends JpaRepository<BusinessReferee, Long> {
    Optional<BusinessReferee> findByApplicationId(Long applicationId);
}
