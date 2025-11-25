package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PartyDetails {

    @NotEmpty(message = ValidationError.PARTY_DETAILS_NULL_EMPTY)
    @Size(max = 40, message = ValidationError.PARTY_DETAILS_MAX_LENGTH)
    private String partyID;

    @NotEmpty(message = ValidationError.PARTY_TYPE_EMPTY)
    @EnumPattern(enumClass = PartyType.class, fieldName = "partyType")
    @Schema(allowableValues = "IND, ORG")
    private String partyType;

    @NotEmpty(message = ValidationError.PARTY_ROLE_EMPTY)
    @Size(max = 40, message = ValidationError.PARTY_ROLE_MAX_LENGTH)
    private String partyRole;

    @Valid
    private IndividualDetails individualDetails;

    @Valid
    private OrganisationDetails organisationDetails;

    @JsonProperty("unavailabilityDOW")
    @Valid
    private List<UnavailabilityDow> unavailabilityDow;

    @Valid
    private List<UnavailabilityRanges> unavailabilityRanges;

    private String partyChannelSubType;
}
