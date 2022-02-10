package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateHearingRequest extends HearingRequest {

    @Valid
    @NotNull(message = ValidationError.INVALID_REQUEST_DETAILS)
    private RequestDetails requestDetails;

    public CreateHearingRequest(HearingDetails hearingDetails, CaseDetails caseDetails,
                                List<PartyDetails> partyDetails, RequestDetails requestDetails) {
        super(hearingDetails, caseDetails, partyDetails);
        this.requestDetails = requestDetails;
    }
}
