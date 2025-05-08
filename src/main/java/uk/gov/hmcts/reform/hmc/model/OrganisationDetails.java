package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class OrganisationDetails {

    @NotEmpty(message = ValidationError.NAME_NULL_EMPTY)
    @Size(max = 2000, message = ValidationError.NAME_MAX_LENGTH)
    @Pattern(regexp = "^[\\p{Ll}\\p{Lm}\\p{Lt}\\p{Lu}\\p{N}\\p{P}\\p{Zs}\\p{Sc}\\p{Sk}\\p{Sm}\\p{Zs}]*$",
        message = ValidationError.INVALID_ORGANISATION_NAME)
    private String name;

    @NotEmpty(message = ValidationError.ORGANISATION_TYPE_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.ORGANISATION_TYPE_MAX_LENGTH)
    private String organisationType;

    @Size(max = 60, message = ValidationError.CFT_ORG_ID_MAX_LENGTH)
    private String cftOrganisationID;
}
