package uk.gov.hmcts.reform.hmc.entity;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.PendingRequestEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_HEARING;

class PendingRequestEntityTest {

    @Test
    void testPendingRequestEntity() {
        final long id = 1L;
        final long hearingId = 12345L;
        final int versionNumber = 1;
        final LocalDateTime submittedDateTime = LocalDateTime.of(2023,10,1,10,1,2);
        final int retryCount = 3;
        final LocalDateTime lastTriedDateTime = LocalDateTime.of(2023,10,2,9,2,3);
        final String status = "PENDING";
        final boolean incidentFlag = true;
        final String message = "Test Message";
        final String messageType = REQUEST_HEARING;
        final String deploymentId = "depIdXX";

        PendingRequestEntity pendingRequest = new PendingRequestEntity();
        pendingRequest.setId(id);
        pendingRequest.setHearingId(hearingId);
        pendingRequest.setVersionNumber(versionNumber);
        pendingRequest.setSubmittedDateTime(submittedDateTime);
        pendingRequest.setRetryCount(retryCount);
        pendingRequest.setLastTriedDateTime(lastTriedDateTime);
        pendingRequest.setStatus(status);
        pendingRequest.setIncidentFlag(incidentFlag);
        pendingRequest.setMessage(message);
        pendingRequest.setMessageType(messageType);
        pendingRequest.setDeploymentId(deploymentId);

        assertThat(id).isEqualTo(pendingRequest.getId());
        assertThat(hearingId).isEqualTo(pendingRequest.getHearingId());
        assertThat(versionNumber).isEqualTo(pendingRequest.getVersionNumber());
        assertThat(submittedDateTime).isEqualTo(pendingRequest.getSubmittedDateTime());
        assertThat(retryCount).isEqualTo(pendingRequest.getRetryCount());
        assertThat(lastTriedDateTime).isEqualTo(pendingRequest.getLastTriedDateTime());
        assertThat(status).isEqualTo(pendingRequest.getStatus());
        assertThat(incidentFlag).isEqualTo(pendingRequest.getIncidentFlag());
        assertThat(message).isEqualTo(pendingRequest.getMessage());
        assertThat(messageType).isEqualTo(pendingRequest.getMessageType());
        assertThat(deploymentId).isEqualTo(pendingRequest.getDeploymentId());

        final String expectedString =
            "id:<" + id + ">,hearingId:<" + hearingId + ">,versionNumber:<" + versionNumber
                + ">,messageType:<" + messageType + ">,submittedDateTime:<"
                + submittedDateTime + ">,retryCount:<" + retryCount + ">,"
            + "lastTriedDateTime:<" + lastTriedDateTime + ">,status:<" + status + ">,incidentFlag:<" + incidentFlag
                + ">,message:<" + message + ">,deploymentId:<" + deploymentId + ">";
        assertThat(expectedString).isEqualTo(pendingRequest.toString());
    }
}
