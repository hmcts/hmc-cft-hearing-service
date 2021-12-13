package uk.gov.hmcts.reform.hmc.model.hmi;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EntityCommunication {

    private String entityCommunicationDetails;

    private String entityCommunicationType;

}
