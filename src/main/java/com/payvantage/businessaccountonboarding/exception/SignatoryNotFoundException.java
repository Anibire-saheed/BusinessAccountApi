package com.payvantage.businessaccountonboarding.exception;

public class SignatoryNotFoundException extends RuntimeException {

    public SignatoryNotFoundException(Long signatoryId) {
        super("No signatory found with id " + signatoryId);
    }
}
