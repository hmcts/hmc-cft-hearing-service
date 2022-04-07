package uk.gov.hmcts.reform.hmc.model.hmi;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class EntityCommunication {

    private List<String> entityCommunicationDetails;

    private String entityCommunicationType;

}
