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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BasePactTesting;
import uk.gov.hmcts.reform.hmc.controllers.HearingManagementController;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestPropertySource("/contract-test.properties")
@ExtendWith(SpringExtension.class)
@Provider(BasePactTesting.PROVIDER_NAME)
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:}",
    consumerVersionSelectors = {@VersionSelector(tag = "master")})
@IgnoreNoPactsToVerify
public class HearingManagementGetHearingsProviderTest extends BasePactTesting {

    @LocalServerPort
    private int port;

    @MockitoBean
    protected HearingManagementService mockService;

    @MockitoBean
    private AccessControlService accessControlService;

    @Mock
    ApplicationParams applicationParams;

    @Mock
    SecurityUtils securityUtils;

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

    @State("hmc cftHearingService successfully returns hearing for valid case ref")
    public void getHearings() {
        final String validCaseRef = "9372710950276233";
        doReturn(generateHearingRequest(validCaseRef)).when(mockService)
                .getHearings(any(), any());
        HearingManagementController controller = new HearingManagementController(mockService,
                                                                                 accessControlService,
                                                                                 applicationParams,
                                                                                 securityUtils);
        GetHearingsResponse getHearingsResponse = controller.getHearings(validCaseRef, null);
        verify(mockService, times(1))
                .getHearings(any(), any());
        Assert.isTrue(getHearingsResponse.getCaseRef().equals(validCaseRef),
                      "Case ref value is not as expected.");
    }

    @State("hmc cftHearingService throws validation error while trying to get hearing for an invalid case ref")
    public void validationErrorForGetHearings() {
        final String validCaseRef = "9372710950276233";
        final String status = "UPDATED"; // for example
        doReturn(generateHearingRequest(validCaseRef)).when(mockService)
                .getHearings(any(), any());
        HearingManagementController controller = new HearingManagementController(mockService,
                                                                                 accessControlService,
                                                                                 applicationParams,
                                                                                 securityUtils);
        GetHearingsResponse getHearingsResponse = controller.getHearings(validCaseRef, status);
        verify(mockService, times(1)).getHearings(any(), any());
        Assert.isTrue(getHearingsResponse.getCaseRef().equals(validCaseRef),
                      "Case ref value is not as expected.");
    }

}
