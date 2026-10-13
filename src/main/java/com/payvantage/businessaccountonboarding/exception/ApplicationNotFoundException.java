package com.payvantage.businessaccountonboarding.exception;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(String applicationUUID) {
        super("No business account application found for UUID " + applicationUUID);
    }
}
