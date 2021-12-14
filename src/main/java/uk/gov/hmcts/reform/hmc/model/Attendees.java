package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attendees {

    @NotEmpty(message = ValidationError.PARTY_ID_NULL_EMPTY)
    @Pattern(regexp = "^\\w{40}$", message = ValidationError.PARTY_ID_MAX_LENGTH)
    private String partyID;

    @NotEmpty(message = ValidationError.HEARING_SUB_CHANNEL_NULL_EMPTY)
    @Pattern(regexp = "^\\w{60}$", message = ValidationError.HEARING_SUB_CHANNEL_MAX_LENGTH)
    private String hearingSubChannel;

}
