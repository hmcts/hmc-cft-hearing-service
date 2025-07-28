package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.exceptions.HearingValidationException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;



@ExtendWith(MockitoExtension.class)
public class ManageExceptionsServiceTest {

    @InjectMocks
    private ManageExceptionsServiceImpl manageExceptionsService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    HearingStatusAuditService hearingStatusAuditService;

    @Mock
    private ObjectMapper objectMapper;

    private static final String CLIENT_S2S_TOKEN = "hmc_tech_admin";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manageExceptionsService = new ManageExceptionsServiceImpl(
            hearingStatusAuditService, hearingRepository,
            objectMapper
        );
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(any(), any(), any(), any(),
                                                                        any(), any(), any());
    }

    @Nested
    @DisplayName("manageExceptions")
    class ManageExceptions {
        @Test
        void shouldThrowExceptionWhenHearingIdLimitExceeds() {
            ManageExceptionRequest request = new ManageExceptionRequest();
            List<SupportRequest> supportRequests = new ArrayList<>();
            for (int i = 0; i < 101; i++) {
                SupportRequest supportRequest = new SupportRequest();
                supportRequest.setHearingId(String.valueOf(i));
                supportRequests.add(supportRequest);
            }
            request.setSupportRequests(supportRequests);
            assertThrows(
                HearingValidationException.class,
                () -> manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN));

        }

        @Test
        void shouldNotThrowExceptionWhenHearingIdLimitIsWithinBounds() {
            ManageExceptionRequest request = new ManageExceptionRequest();
            List<SupportRequest> supportRequests = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                SupportRequest supportRequest = new SupportRequest();
                supportRequest.setHearingId(String.valueOf(i));
                supportRequests.add(supportRequest);
            }
            request.setSupportRequests(supportRequests);
            manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN);
        }

        @Test
        void validateUniqueHearingIds_shouldThrowExceptionOnDuplicateIds() {
            List<SupportRequest> supportRequests = new ArrayList<>();
            SupportRequest req1 = new SupportRequest();
            req1.setHearingId("2000000000");
            SupportRequest req2 = new SupportRequest();
            req2.setHearingId("2000000000"); // duplicate
            supportRequests.add(req1);
            supportRequests.add(req2);
            ManageExceptionRequest request = new ManageExceptionRequest();
            request.setSupportRequests(supportRequests);

            assertThrows(HearingValidationException.class, () ->
                manageExceptionsService.manageExceptions(request, CLIENT_S2S_TOKEN)
            );
        }
    }
}
