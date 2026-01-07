package uk.gov.hmcts.reform.hmc.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

@Builder
@Data
public class HearingStatusAuditContext {
    private HearingEntity hearingEntity;
    private String hearingEvent;
    private String httpStatus;
    private String source;
    private String target;
    private JsonNode errorDetails;
    private JsonNode otherInfo;
    private boolean hearingState;

}
