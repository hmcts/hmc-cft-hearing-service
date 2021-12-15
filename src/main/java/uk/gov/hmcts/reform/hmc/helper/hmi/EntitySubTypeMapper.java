package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.EntitySubType;

@Component
public class EntitySubTypeMapper {

    private static final String ORGANISATION_CLASS_CODE = "ORG/ORG";
    private static final String PERSON_CLASS_CODE = "IND/PERSON";

    public EntitySubType getPersonEntitySubType(PartyDetails partyDetails) {
        return EntitySubType.builder()
            .entityTitle(partyDetails.getIndividualDetails().getTitle())
            .entityFirstName(partyDetails.getIndividualDetails().getFirstName())
            .entityLastName(partyDetails.getIndividualDetails().getLastName())
            .entityInterpreterLanguage(partyDetails.getIndividualDetails().getInterpreterLanguage())
            .entityClassCode(PERSON_CLASS_CODE)
            .entitySensitiveClient(partyDetails.getIndividualDetails().getVulnerableFlag())
            .entityAlertMessage(partyDetails.getIndividualDetails().getVulnerabilityDetails())
            .build();
    }

    public EntitySubType getOrgEntitySubType(PartyDetails partyDetails) {
        return EntitySubType.builder()
            .entityCompanyName(partyDetails.getOrganisationDetails().getName())
            .entityClassCode(ORGANISATION_CLASS_CODE)
            .build();
    }
}
