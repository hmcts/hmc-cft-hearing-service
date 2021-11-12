package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @NotNull(message = ValidationError.INVALID_REQUEST_DETAILS)
    @JsonProperty("requestDetails")
    private RequestDetails requestDetails;

    @Valid
    @NotNull(message = ValidationError.INVALID_HEARING_DETAILS)
    @JsonProperty("hearingDetails")
    private HearingDetails hearingDetails;

    @Valid
    @NotNull(message = ValidationError.INVALID_CASE_DETAILS)
    @JsonProperty("caseDetails")
    private CaseDetails caseDetails;

    @Valid
    @JsonProperty("partyDetails")
    private List<PartyDetails> partyDetails;

}
