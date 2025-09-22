package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.PendingRequestEntity;
import uk.gov.hmcts.reform.hmc.repository.PendingRequestRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PendingRequestServiceImpl implements PendingRequestService {

    private final PendingRequestRepository pendingRequestRepository;

    public PendingRequestServiceImpl(PendingRequestRepository pendingRequestRepository) {
        this.pendingRequestRepository = pendingRequestRepository;
    }

    public void generatePendingRequest(JsonNode message, Long hearingId, String messageType, String deploymentId) {
        log.debug("generatePendingRequest(message, applicationProperties)");
        PendingRequestEntity pendingRequest = newPendingRequestEntity(message, hearingId, messageType, deploymentId);
        try {
            log.debug("saving pendingRequest: {}", pendingRequest);
            pendingRequestRepository.save(pendingRequest);
        } catch (Exception e) {
            log.error("Failed to generate pending request", e);
        }
    }

    public PendingRequestEntity findById(Long id) {
        log.debug("findById({})", id);
        return pendingRequestRepository.findById(id).orElse(null);
    }

    private PendingRequestEntity newPendingRequestEntity(JsonNode message, Long hearingId, String messageType,
                                                         String deploymentId) {
        PendingRequestEntity pendingRequest = new PendingRequestEntity();
        pendingRequest.setId(0L);
        pendingRequest.setHearingId(hearingId);
        pendingRequest.setMessage(message.toString());
        pendingRequest.setMessageType(messageType);
        pendingRequest.setStatus("PENDING");
        pendingRequest.setIncidentFlag(false);
        pendingRequest.setVersionNumber(1);

        LocalDateTime currentTimestamp = LocalDateTime.now();
        pendingRequest.setSubmittedDateTime(currentTimestamp);
        pendingRequest.setRetryCount(0);
        if (null == deploymentId) {
            deploymentId = "";
        }
        pendingRequest.setDeploymentId(deploymentId);

        return pendingRequest;
    }

}
