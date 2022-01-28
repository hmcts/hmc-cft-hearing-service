package uk.gov.hmcts.reform.hmc.model;

import lombok.*;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;

@Data
@NoArgsConstructor
@Setter
@Getter
public class GetHearingResponse {

    RequestDetails requestDetails;
    HearingDetails hearingDetails;
    CaseDetails caseDetails;
    PartyDetails partyDetails;
    HearingResponse hearingResponse;

}
