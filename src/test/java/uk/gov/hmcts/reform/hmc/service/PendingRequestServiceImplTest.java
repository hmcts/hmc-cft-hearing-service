package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.PendingRequestEntity;
import uk.gov.hmcts.reform.hmc.repository.PendingRequestRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_HEARING;

@Nested
@DisplayName("PendingRequestServiceImpl")
class PendingRequestServiceImplTest {

    @InjectMocks
    private PendingRequestServiceImpl pendingRequestService;

    @Mock
    private PendingRequestRepository pendingRequestRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAddToPendingRequestsSuccessfully() {
        JsonNode message;
        try {
            message = new ObjectMapper().readTree("{\"message\": \"Test message\"}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        final long hearingId = 1L;
        final String message_type = REQUEST_HEARING;
        final String deploymentId = "depIdXXX";

        PendingRequestEntity pendingRequest = new PendingRequestEntity();
        pendingRequest.setId(1L);
        when(pendingRequestRepository.save(any(PendingRequestEntity.class))).thenReturn(pendingRequest);

        pendingRequestService.addToPendingRequests(message, hearingId, message_type, deploymentId);

        verify(pendingRequestRepository, times(1)).save(any(PendingRequestEntity.class));
    }
}
