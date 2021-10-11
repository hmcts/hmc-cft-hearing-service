package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class PartyDetails {

    @NotEmpty(message = ValidationError.PARTY_DETAILS_NULL_EMPTY)
    @Size(max = 40, message = ValidationError.PARTY_DETAILS_MAX_LENGTH)
    private String partyID;

    private PartyType partyType;

    @Size(max = 6, message = ValidationError.PARTY_ROLE_MAX_LENGTH)
    private String partyRole;

    @Valid
    private IndividualDetails individualDetails;

    @Valid
    private OrganisationDetails organisationDetails;

    @JsonProperty("unavailabilityDOW")
    private UnavailabilityDow[] unavailabilityDow;

    private UnavailabilityRanges[] unavailabilityRanges;
}
