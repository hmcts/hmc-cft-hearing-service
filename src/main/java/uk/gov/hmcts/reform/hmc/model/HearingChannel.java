package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingChannel {

    @Valid
    @NotEmpty(message = ValidationError.MISSING_CHANNEL_TYPE)
    @Size(max = 70, message = ValidationError.CHANNEL_TYPE_MAX_LENGTH)
    private String channelType;
}
