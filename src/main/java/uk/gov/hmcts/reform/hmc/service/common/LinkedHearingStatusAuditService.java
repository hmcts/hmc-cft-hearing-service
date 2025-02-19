package uk.gov.hmcts.reform.hmc.service.common;


import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;

import java.util.List;

public interface LinkedHearingStatusAuditService {

    void saveLinkedHearingAuditTriageDetails(String source, LinkedGroupDetails linkedGroupDetails,
                                             String hearingEvent, String httpStatus, String target,
                                             JsonNode errorDesc, List<HearingEntity> hearingEntities);
}
