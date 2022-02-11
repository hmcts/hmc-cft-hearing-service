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
public class UpdateHearingRequest extends HearingRequest {

    @Valid
    @NotNull(message = ValidationError.INVALID_REQUEST_DETAILS)
    private UpdateRequestDetails requestDetails;

    public UpdateHearingRequest(HearingDetails hearingDetails,  CaseDetails caseDetails,
                                List<PartyDetails> partyDetails, UpdateRequestDetails requestDetails) {
        super(hearingDetails, caseDetails, partyDetails);
        this.requestDetails = requestDetails;
    }
}
