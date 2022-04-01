package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;

import java.util.List;

@Data
@NoArgsConstructor
@Setter
@Getter
public class GetHearingResponse {

    private RequestDetails requestDetails;
    private HearingDetails hearingDetails;
    private CaseDetails caseDetails;
    private List<PartyDetails> partyDetails;
    private HearingResponse hearingResponse;
}
