package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class HearingCreatePutResponse extends HearingCreatePostResponse {

    @NotEmpty(message = ValidationError.VERSION_NUMBER_NULL_EMPTY)
    @Pattern(regexp = "^\\w{100}$", message = ValidationError.VERSION_NUMBER_MAX_LENGTH)
    private String versionNumber;

}
