package uk.gov.hmcts.reform.hmc.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

@Data
@NoArgsConstructor
public class RelatedParty {

    @NotEmpty(message = ValidationError.RELATED_PARTY_EMPTY)
    @Size(max = 15, message = ValidationError.RELATED_PARTY_MAX_LENGTH)
    private String relatedPartyID;

    @NotEmpty(message = ValidationError.RELATIONSHIP_TYPE_EMPTY)
    @Size(max = 10, message = ValidationError.RELATIONSHIP_TYPE_MAX_LENGTH)
    private String relationshipType;

}
