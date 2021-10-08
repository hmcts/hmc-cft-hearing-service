package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class RelatedParty {

    @NotNull(message = ValidationError.RELATED_PARTY_EMPTY)
    @Size(max = 2000, message = ValidationError.RELATED_PARTY_MAX_LENGTH)
    private String relatedPartyID;

    @NotNull(message = ValidationError.RELATION_SHIP_TYPE_EMPTY)
    @Size(max = 2000, message = ValidationError.RELATION_SHIP_TYPE_MAX_LENGTH)
    private String relationshipType;

}
