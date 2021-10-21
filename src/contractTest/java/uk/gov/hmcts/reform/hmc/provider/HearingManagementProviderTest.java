package uk.gov.hmcts.reform.hmc.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.netflix.config.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.hmc.Application;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {
    Application.class, TestingUtil.class
})
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@TestPropertySource("/contract-test.properties")

@ExtendWith(SpringExtension.class)
@Provider("ccd")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}",
    consumerVersionSelectors = {@VersionSelector(tag = "HMAN-25")})
//@ContextConfiguration(classes = {ContractConfig.class, MapperConfig.class})
@IgnoreNoPactsToVerify
public class HearingManagementProviderTest {

    @LocalServerPort
    private int port;

    @MockBean
    protected HearingManagementService mockService;

    @Value("${pact.verifier.publishResults:false}")
    private String publishResults;

    @BeforeEach
    public void setup(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", publishResults);
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    @AfterEach
    public void teardown(PactVerificationContext context) {
        reset(mockService);
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    public void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("HMC successfully creates hearing")
    public void createHearing() {
        HearingRequest hearingRequest = TestingUtil.getHearingRequest();
        doNothing().when(mockService).validateHearingRequest(hearingRequest);
        // check hearing created
    }

    @State("HMC throws validation error for create Hearing")
    public void validationErrorForCreatingHearing() {
        HearingRequest hearingRequest = TestingUtil.getHearingRequest();
        hearingRequest.setCaseDetails(null);
        doThrow(new ValidationException(ValidationError.INVALID_HEARING_REQUEST_DETAILS))
            .when(mockService).validateHearingRequest(hearingRequest);
    }

}
