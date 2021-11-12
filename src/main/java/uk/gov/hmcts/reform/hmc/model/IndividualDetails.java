package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG;

@Data
@NoArgsConstructor
public class IndividualDetails {

    @NotNull(message = ValidationError.TITLE_EMPTY)
    @Size(max = 40, message = ValidationError.TITLE_MAX_LENGTH)
    @JsonProperty("title")
    private String title;

    @NotNull(message = ValidationError.FIRST_NAME_EMPTY)
    @Size(max = 100, message = ValidationError.FIRST_NAME_MAX_LENGTH)
    @JsonProperty("firstName")
    private String firstName;

    @NotNull(message = ValidationError.LAST_NAME_EMPTY)
    @Size(max = 100, message = ValidationError.LAST_NAME_MAX_LENGTH)
    @JsonProperty("lastName")
    private String lastName;

    @Size(max = 70, message = ValidationError.PREFERRED_HEARING_CHANNEL_MAX_LENGTH)
    @JsonProperty("preferredHearingChannel")
    private String preferredHearingChannel;

    @Size(max = 10, message = ValidationError.INTERPRETER_LANGUAGE_MAX_LENGTH)
    @JsonProperty("interpreterLanguage")
    private String interpreterLanguage;

    @JsonProperty("reasonableAdjustments")
    private List<@Size(max = 10, message = REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG) String> reasonableAdjustments;

    @JsonProperty("vulnerableFlag")
    private Boolean vulnerableFlag;

    @Size(max = 256, message = ValidationError.VULNERABLE_DETAILS_MAX_LENGTH)
    @JsonProperty("vulnerabilityDetails")
    private String vulnerabilityDetails;

    @Size(max = 120, message = ValidationError.HEARING_CHANNEL_EMAIL_MAX_LENGTH)
    @Email(message = ValidationError.HEARING_CHANNEL_EMAIL_INVALID)
    @JsonProperty("hearingChannelEmail")
    private String hearingChannelEmail;

    @Size(max = 30, message = ValidationError.HEARING_CHANNEL_PHONE_MAX_LENGTH)
    @Pattern(regexp = "^\\+?(?:[0-9] ?){6,14}[0-9]$",
        message = ValidationError.HEARING_CHANNEL_PHONE_INVALID)
    @JsonProperty("hearingChannelPhone")
    private String hearingChannelPhone;

    @Valid
    @JsonProperty("relatedParties")
    private List<RelatedParty> relatedParties;

}
