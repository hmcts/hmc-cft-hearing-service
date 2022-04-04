package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingRequest {

    @Valid
    @NotNull(message = ValidationError.INVALID_HEARING_DETAILS)
    private HearingDetails hearingDetails;

    @Valid
    @NotNull(message = ValidationError.INVALID_CASE_DETAILS)
    private CaseDetails caseDetails;

    @Valid
    private List<PartyDetails> partyDetails;

    public RequestDetails getRequestDetails() {
        return null;
    }
}
