package uk.gov.hmcts.reform.hmc.config.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MultiIssuerValidatorTest {

    private static final String ISSUER_VALID = "http://validIssuer";
    private static final String ISSUER_VALID_OTHER = "http://otherValidIssuer";
    private static final String ISSUER_EMPTY = "";
    private static final String ISSUER_INVALID = "http://invalidIssuer";

    private static final String AUTH_ERROR_INVALID_TOKEN = "invalid_token";

    private static final String ZONE_EUROPE_LONDON = "Europe/London";
    private static final String TOKEN_VALUE = "testTokenValue";

    @Test
    void initialisationShouldFailWithNullIssuer() {
        assertThrows(NullPointerException.class,
                     () -> new MultiIssuerValidator((String) null),
                     "Exception should be thrown if issuer is null");
    }

    @ParameterizedTest
    @MethodSource("arraysWithNullIssuer")
    void initialisationShouldFailWithNullIssuerInArray(String[] issuers) {
        assertThrows(NullPointerException.class,
                     () -> new MultiIssuerValidator(issuers),
                     "Exception should be thrown if array contains a null issuer");
    }

    @Test
    void initialisationShouldFailWithEmptyArray() {
        String[] emptyArray = {};
        assertThrows(IllegalArgumentException.class,
                     () -> new MultiIssuerValidator(emptyArray),
                     "Exception should be thrown if array is empty");
    }

    @Test
    void initialisationShouldSucceedWithNonNullIssuer() {
        assertDoesNotThrow(() -> new MultiIssuerValidator(ISSUER_VALID),
                           "Exception should not be thrown for non-null issuer");
    }

    @Test
    void initialisationShouldSucceedWithNonNullIssuerInArray() {
        String[] validIssuerArray = {ISSUER_VALID};
        assertDoesNotThrow(() -> new MultiIssuerValidator(validIssuerArray),
                           "Exception should not be thrown for non-null, non-empty array of non-null issuers");
    }

    @Test
    void validateShouldFailForNoIssuer() {
        MultiIssuerValidator validator = new MultiIssuerValidator(ISSUER_VALID);

        Jwt jwtWithoutIssuer = createJwtWithoutIssuer();
        OAuth2TokenValidatorResult result = validator.validate(jwtWithoutIssuer);

        assertValidateFailure(result);
    }

    @Test
    void validateShouldFailForEmptyIssuer() {
        MultiIssuerValidator validator = new MultiIssuerValidator(ISSUER_VALID);

        Jwt jwtWithEmptyIssuer = createJwt(ISSUER_EMPTY);
        OAuth2TokenValidatorResult result = validator.validate(jwtWithEmptyIssuer);

        assertValidateFailure(result);
    }

    @Test
    void validateShouldFailForInvalidIssuer() {
        MultiIssuerValidator validator = new MultiIssuerValidator(ISSUER_VALID);

        Jwt jwtIssuerInvalid = createJwt(ISSUER_INVALID);
        OAuth2TokenValidatorResult result = validator.validate(jwtIssuerInvalid);

        assertValidateFailure(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {ISSUER_VALID_OTHER, ISSUER_VALID})
    void validateShouldSucceed(String issuer) {
        MultiIssuerValidator validator = new MultiIssuerValidator(ISSUER_VALID, ISSUER_VALID_OTHER);

        Jwt jwtValid = createJwt(issuer);
        OAuth2TokenValidatorResult result = validator.validate(jwtValid);

        assertValidateSuccess(result);
    }

    private static Stream<Arguments> arraysWithNullIssuer() {
        String[] onlyNullIssuer = {null};
        String[] nonNullAndNullIssuer = {ISSUER_VALID, null};

        return Stream.of(
            arguments((Object) onlyNullIssuer),
            arguments((Object) nonNullAndNullIssuer)
        );
    }

    private void assertValidateFailure(OAuth2TokenValidatorResult result) {
        assertTrue(result.hasErrors(), "Validator result should contain errors");

        Collection<OAuth2Error> errors = result.getErrors();
        assertEquals(1, errors.size(), "Validator result has unexpected number of errors");

        Optional<OAuth2Error> errorOptional = errors.stream().findFirst();
        assertTrue(errorOptional.isPresent(), "Validator result error should be present");

        OAuth2Error error = errorOptional.get();
        assertEquals(AUTH_ERROR_INVALID_TOKEN,
                     error.getErrorCode(),
                     "Validator result error has unexpected error code");
        assertEquals("The issuer is missing or invalid",
                     error.getDescription(),
                     "Validator result error has unexpected description");
    }

    private void assertValidateSuccess(OAuth2TokenValidatorResult result) {
        assertFalse(result.hasErrors(), "Validator result should not contain errors");
    }

    private Jwt createJwtWithoutIssuer() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "Bearer");

        return createJwt(claims);
    }

    private Jwt createJwt(String issuer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", issuer);
        claims.put("token_type", "Bearer");

        return createJwt(claims);
    }

    private Jwt createJwt(Map<String, Object> claims) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        Instant issuedAt = currentDateTime.atZone(ZoneId.of(ZONE_EUROPE_LONDON)).toInstant();
        Instant expiresAt = currentDateTime.plusHours(8L).atZone(ZoneId.of(ZONE_EUROPE_LONDON)).toInstant();

        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");

        return new Jwt(TOKEN_VALUE, issuedAt, expiresAt, headers, claims);
    }
}
