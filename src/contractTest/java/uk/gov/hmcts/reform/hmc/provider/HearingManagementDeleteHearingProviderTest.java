package uk.gov.hmcts.reform.hmc.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.hmc.BasePactTesting;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@TestPropertySource("/contract-test.properties")
@ExtendWith(SpringExtension.class)
@Provider(BasePactTesting.PROVIDER_NAME)
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:}",
    consumerVersionSelectors = {@VersionSelector(tag = "master")})
@IgnoreNoPactsToVerify
public class HearingManagementDeleteHearingProviderTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    protected HearingManagementService mockService;

    @Value("${pact.verifier.publishResults:false}")
    private String publishResults;

    @BeforeEach
    public void setup(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", publishResults);
        if (null != context) {
            context.setTarget(new HttpTestTarget("localhost", port, "/"));
        }
    }

    @AfterEach
    public void teardown(PactVerificationContext context) {
        reset(mockService);
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    public void pactVerificationTestTemplate(PactVerificationContext context) {
        if (null != context) {
            context.verifyInteraction();
        }
    }

    @State("hmcCftHearingService successfully acknowledges delete hearing")
    public void deleteHearing() {
        doNothing().when(mockService).deleteHearingRequest(any(Long.class), any(DeleteHearingRequest.class), any());
    }

    @State("hmcCftHearingService throws validation error while trying to delete hearing")
    public void validationErrorForDeleteHearing() {
        doThrow(new Exception(ValidationError.INVALID_HEARING_REQUEST_DETAILS))
            .when(mockService).deleteHearingRequest(any(Long.class), any(DeleteHearingRequest.class), any());
    }

}
