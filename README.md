# Business Account Onboarding API

Backend for the RC / BN / IT business account opening flow (reference frontend: business-acount-form.vercel.app).

## Running locally

```
./mvnw spring-boot:run
```

Requires a MySQL database (see `src/main/resources/application.properties` for connection defaults) and AWS credentials with S3 access for document uploads (`aws.access.key`, `aws.secret.key`, `aws.s3.bucket`).

## API documentation

A Postman collection and local environment are in [`postman/`](postman/):

- `Business-Account-Onboarding.postman_collection.json`
- `Business-Account-Onboarding.postman_environment.json`

Import both into Postman, select the "Business Account Onboarding - Local" environment, and run requests in order (1 → 14). Running "1. Create Application" auto-saves `applicationUUID` into a collection variable that every later request reuses, and "7. Add Principal - Admin Officer" does the same for `principalId`.

## Known gaps

- `SecurityConfig` currently `permitAll()`s every request — no JWT filter is wired up yet. Port `JwtFilter`/`JwtAuthEntryPoint` from `ethica-internet-banking-api`'s `security` package before deploying beyond local development.
- `POST /business-account/applications` takes `applicantUserUUID` as a plain request param until real authentication exists.
