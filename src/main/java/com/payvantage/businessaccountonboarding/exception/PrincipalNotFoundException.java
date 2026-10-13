package com.payvantage.businessaccountonboarding.exception;

public class PrincipalNotFoundException extends RuntimeException {

    public PrincipalNotFoundException(Long principalId) {
        super("No principal found with id " + principalId);
    }
}
