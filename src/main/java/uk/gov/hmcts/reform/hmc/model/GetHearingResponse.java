package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GetHearingResponse {

    RequestDetails requestDetails;
    HearingDetails hearingDetails;
    CaseDetails caseDetails;
    ArrayList<PartyDetails> partyDetails;
    ArrayList<HearingResponse> hearingResponse;

}
