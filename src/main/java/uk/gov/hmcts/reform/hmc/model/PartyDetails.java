package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class PartyDetails {

    @NotEmpty(message = ValidationError.PARTY_DETAILS_NULL_EMPTY)
    @Size(max = 40, message = ValidationError.PARTY_DETAILS_MAX_LENGTH)
    @JsonProperty("partyID")
    private String partyID;

    @EnumPattern(enumClass = PartyType.class, fieldName = "partyType")
    @JsonProperty("partyType")
    private String partyType;

    @Size(max = 6, message = ValidationError.PARTY_ROLE_MAX_LENGTH)
    @JsonProperty("partyRole")
    private String partyRole;

    @Valid
    @JsonProperty("individualDetails")
    private IndividualDetails individualDetails;

    @Valid
    @JsonProperty("organisationDetails")
    private OrganisationDetails organisationDetails;

    @JsonProperty("unavailabilityDOW")
    @Valid
    private List<UnavailabilityDow> unavailabilityDow;

    @Valid
    @JsonProperty("unavailabilityRanges")
    private List<UnavailabilityRanges> unavailabilityRanges;
}
