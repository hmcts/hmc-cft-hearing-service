package uk.gov.hmcts.reform.hmc.config.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
public class MultiIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final List<String> validIssuers;

    private final OAuth2Error errorIssuerInvalid =
        new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "The issuer is missing or invalid", null);

    public MultiIssuerValidator(String... validIssuers) {
        List<String> issuers = List.of(validIssuers);
        Assert.notEmpty(issuers, "Valid issuers should not be null or empty");
        this.validIssuers = issuers;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String issuer = jwt.getClaimAsString(JwtClaimNames.ISS) == null ? "" : jwt.getClaimAsString(JwtClaimNames.ISS);

        if (!issuer.isEmpty() && validIssuers.contains(issuer)) {
            return OAuth2TokenValidatorResult.success();
        } else {
            log.error("Invalid issuer: [{}]", issuer);
            return OAuth2TokenValidatorResult.failure(errorIssuerInvalid);
        }
    }
}
