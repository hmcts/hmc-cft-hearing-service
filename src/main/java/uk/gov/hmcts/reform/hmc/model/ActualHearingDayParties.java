package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ActualHearingDayParties implements Serializable {

    @Size(max = 40, message = ValidationError.HA_HEARING_DAY_PARTY_ID_MAX_LENGTH)
    private String actualPartyId;

    @NotEmpty(message = ValidationError.HA_HEARING_DAY_PARTY_ROLE_NOT_EMPTY)
    @Size(max = 40, message = ValidationError.HA_HEARING_DAY_PARTY_ROLE_MAX_LENGTH)
    private String partyRole;

    @Valid
    private ActualHearingDayPartyDetail individualDetails;

    @Size(max = 200, message = ValidationError.HA_HEARING_DAY_ORGANISATION_NAME_MAX_LENGTH)
    private String actualOrganisationName;

    @NotEmpty(message = ValidationError.HA_HEARING_DAY_PARTY_CHANNEL_NOT_EMPTY)
    @Size(max = 70, message = ValidationError.HA_HEARING_DAY_PARTY_CHANNEL_MAX_LENGTH)
    private String partyChannelSubType;

    private Boolean didNotAttendFlag = false;

    @Size(max = 40, message = ValidationError.HA_HEARING_DAY_REPRESENTED_PARTY_MAX_LENGTH)
    private String representedParty;
}
