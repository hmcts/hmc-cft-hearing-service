package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.EntitySubType;

@Component
public class EntitySubTypeMapper {

    public EntitySubType getEntitySubType(PartyDetails partyDetails) {
        return EntitySubType.builder()
            .entityTitle(partyDetails.getIndividualDetails().getTitle())
            .entityFirstName(partyDetails.getIndividualDetails().getFirstName())
            .entityLastName(partyDetails.getIndividualDetails().getLastName())
            .entityInterpreterLanguage(partyDetails.getIndividualDetails().getInterpreterLanguage())
            .entityClassCode("IND/PERSON")
            .entitySensitiveClient(partyDetails.getIndividualDetails().getVulnerableFlag())
            .entityAlertMessage(partyDetails.getIndividualDetails().getVulnerabilityDetails())
            .build();
    }
}
