package uk.gov.hmcts.reform.hmc.service;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    void shouldGeneratePendingRequestSuccessfully() {
        JsonNode message = new ObjectMapper().createObjectNode().put("message", "Test message");

        final long hearingId = 1L;
        final String message_type = REQUEST_HEARING;
        final String deploymentId = "depIdXXX";

        PendingRequestEntity pendingRequest = new PendingRequestEntity();
        pendingRequest.setId(1L);
        when(pendingRequestRepository.save(any(PendingRequestEntity.class))).thenReturn(pendingRequest);

        pendingRequestService.generatePendingRequest(message, hearingId, message_type, deploymentId);

        verify(pendingRequestRepository, times(1)).save(any(PendingRequestEntity.class));
    }

    @Test
    @DisplayName("Should return pending request when ID exists")
    void shouldReturnPendingRequestWhenIdExists() {
        final long id = 1L;
        PendingRequestEntity pendingRequest = new PendingRequestEntity();
        pendingRequest.setId(id);
        when(pendingRequestRepository.findById(id)).thenReturn(Optional.of(pendingRequest));

        PendingRequestEntity result = pendingRequestService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should return null when ID does not exist")
    void shouldReturnNullWhenIdDoesNotExist() {
        final long id = 1L;
        when(pendingRequestRepository.findById(id)).thenReturn(Optional.empty());

        PendingRequestEntity result = pendingRequestService.findById(id);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null ID gracefully")
    void shouldHandleNullIdGracefully() {
        PendingRequestEntity result = pendingRequestService.findById(null);

        assertThat(result).isNull();
    }
}
