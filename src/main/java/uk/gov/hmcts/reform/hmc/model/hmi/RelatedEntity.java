package uk.gov.hmcts.reform.hmc.model.hmi;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RelatedEntity {

    private String relatedEntityId;

    private String relatedEntityRelationshipType;

}
