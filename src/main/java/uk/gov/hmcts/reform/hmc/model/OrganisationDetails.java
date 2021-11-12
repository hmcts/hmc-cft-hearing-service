package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class OrganisationDetails {

    @NotEmpty(message = ValidationError.NAME_NULL_EMPTY)
    @Size(max = 2000, message = ValidationError.NAME_MAX_LENGTH)
    @JsonProperty("name")
    private String name;

    @NotEmpty(message = ValidationError.ORGANISATION_TYPE_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.ORGANISATION_TYPE_MAX_LENGTH)
    @JsonProperty("organisationType")
    private String organisationType;

    @NotEmpty(message = ValidationError.CFT_ORG_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.CFT_ORG_ID_MAX_LENGTH)
    @JsonProperty("cftOrganisationID")
    private String cftOrganisationID;
}
