package uk.gov.hmcts.reform.hmc.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.Instant;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.utils.KeyGenerator.getRsaJwk;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
class JwtDecoderIT {
    // Note: Class cannot inherit from BaseTest as security filters need to be enabled for tests

    private static final String URL_HEARING = "/hearing";
    private static final String REQUEST_HEADER_SERVICE_AUTHORISATION = "ServiceAuthorization";
    private static final String REQUEST_HEADER_AUTHORISATION = "Authorization";
    private static final String RESPONSE_HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    private static final String CCD_GET_CASE_DETAILS_RESPONSE = """
        {
            "jurisdiction": "Jurisdiction1",
            "case_type": "CaseType1"
        }""";

    private static final String INVALID_ISSUER = "http://invalidIssuer";

    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String GET_HEARINGS_DATA_SCRIPT = "classpath:sql/get-caseHearings_request.sql";

    private final MockMvc mockMvc;

    private static final String VALID_ISSUER_WEB_PUBLIC = "http://localhost:5000/o";
    private static final String VALID_ISSUER_FORGEROCK = "http://fr-am:8080/openam/oauth2/hmcts";

    @Autowired
    public JwtDecoderIT(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUp() {
        stubFor(WireMock.get("/s2s/details")
                    .withHeader(REQUEST_HEADER_AUTHORISATION, WireMock.equalTo("Bearer 1234"))
                    .willReturn(WireMock.ok("xui_webapp")));
    }

    @Test
    void jwtTimestampValidator_shouldFailIfExpiresAtInPast() throws Exception {
        Instant currentInstant = Instant.now();
        Instant issuedAt = currentInstant.minusSeconds(3600);
        Instant expiresAt = currentInstant.minusSeconds(1800);
        Instant notBefore = currentInstant.minusSeconds(3600);

        String userToken = createUserToken(issuedAt, expiresAt, notBefore);

        RequestBuilder getHearingRequest = createGetHearingRequest(userToken);
        mockMvc.perform(getHearingRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(RESPONSE_HEADER_WWW_AUTHENTICATE))
            .andExpect(header().string(RESPONSE_HEADER_WWW_AUTHENTICATE, containsString("Jwt expired at")));
    }

    @Test
    void jwtTimestampValidator_shouldFailIfNotBeforeInFuture() throws Exception {
        Instant currentInstant = Instant.now();
        Instant issuedAt = currentInstant.minusSeconds(3600);
        Instant expiresAt = currentInstant.plusSeconds(3600);
        Instant notBefore = currentInstant.plusSeconds(1800);

        String userToken = createUserToken(issuedAt, expiresAt, notBefore);

        RequestBuilder getHearingRequest = createGetHearingRequest(userToken);
        mockMvc.perform(getHearingRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(RESPONSE_HEADER_WWW_AUTHENTICATE))
            .andExpect(header().string(RESPONSE_HEADER_WWW_AUTHENTICATE, containsString("Jwt used before")));
    }

    @Test
    void multiIssuerValidator_shouldFailIfIssuerInvalid() throws Exception {
        String userToken = createUserToken(INVALID_ISSUER);

        RequestBuilder getHearingRequest = createGetHearingRequest(userToken);
        mockMvc.perform(getHearingRequest)
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(RESPONSE_HEADER_WWW_AUTHENTICATE))
            .andExpect(header().string(RESPONSE_HEADER_WWW_AUTHENTICATE,
                                       containsString("The issuer is missing or invalid")));
    }

    @ParameterizedTest
    @ValueSource(strings = {VALID_ISSUER_FORGEROCK, VALID_ISSUER_WEB_PUBLIC})
    @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, GET_HEARINGS_DATA_SCRIPT})
    void multiIssuerValidator_shouldSucceedWithValidIssuer(String validIssuer) throws Exception {
        stubCcdGetCaseDetails();

        String userToken = createUserToken(validIssuer);

        RequestBuilder getHearingRequest = createGetHearingRequest(userToken);
        mockMvc.perform(getHearingRequest).andExpect(status().isOk());
    }

    private void stubCcdGetCaseDetails() {
        stubFor(WireMock.get(WireMock.urlMatching("/cases/.*"))
                    .willReturn(WireMock.okJson(CCD_GET_CASE_DETAILS_RESPONSE)));
    }

    private String createUserToken(String validIssuer) throws JOSEException {
        Instant currentInstant = Instant.now();
        Instant issuedAt = currentInstant.minusSeconds(3600);
        Instant expiresAt = currentInstant.plusSeconds(3600);
        Instant notBefore = currentInstant.minusSeconds(3600);

        return createUserToken(validIssuer, issuedAt, expiresAt, notBefore);
    }

    private String createUserToken(Instant issuedAt, Instant expiresAt, Instant notBefore) throws JOSEException {
        return createUserToken(VALID_ISSUER_FORGEROCK, issuedAt, expiresAt, notBefore);
    }

    private String createUserToken(String issuer, Instant issuedAt, Instant expiresAt, Instant notBefore)
        throws JOSEException {

        // Have to use keys created by KeyGenerator test utility class because WireMock
        // is configured to return those details when JWKS are requested.
        String userToken =
            JWT.create()
                .withHeader(Map.of("typ", "JWT",
                                   "alg", "RS256",
                                   "kid", "23456789"))
                .withSubject("hmc_cft_hearing_service")
                .withIssuer(issuer)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withNotBefore(notBefore)
                .sign(Algorithm.RSA256(getRsaJwk().toRSAPublicKey(), getRsaJwk().toRSAPrivateKey()));

        return "Bearer " + userToken;
    }

    private RequestBuilder createGetHearingRequest(String userToken) {
        return get(URL_HEARING + "/2000000000")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header(REQUEST_HEADER_SERVICE_AUTHORISATION, "1234")
            .header(REQUEST_HEADER_AUTHORISATION, userToken);
    }
}
