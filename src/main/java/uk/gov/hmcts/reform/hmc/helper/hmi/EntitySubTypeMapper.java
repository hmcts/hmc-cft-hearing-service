package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.EntitySubType;

@Component
public class EntitySubTypeMapper {

    public static final String ORGANISATION_CLASS_CODE = "ORG";
    public static final String PERSON_CLASS_CODE = "PERSON";

    public EntitySubType getPersonEntitySubType(IndividualDetails individualDetails) {
        return EntitySubType.builder()
            .entityTitle(individualDetails.getTitle())
            .entityFirstName(individualDetails.getFirstName())
            .entityLastName(individualDetails.getLastName())
            .entityInterpreterLanguage(individualDetails.getInterpreterLanguage())
            .entityClassCode(PERSON_CLASS_CODE)
            .entitySensitiveClient(individualDetails.getVulnerableFlag())
            .entityAlertMessage(individualDetails.getVulnerabilityDetails())
            .entitySpecialNeedsOther(individualDetails.getOtherReasonableAdjustmentDetails())
            .entityCustodyStatus(individualDetails.getCustodyStatus())
            .build();
    }

    public EntitySubType getOrgEntitySubType(OrganisationDetails organisationDetails) {
        return EntitySubType.builder()
            .entityCompanyName(organisationDetails.getName())
            .entityClassCode(ORGANISATION_CLASS_CODE)
            .build();
    }
}
