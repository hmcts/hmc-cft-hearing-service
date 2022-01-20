package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.EntitySubType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.helper.hmi.EntitySubTypeMapper.ORGANISATION_CLASS_CODE;
import static uk.gov.hmcts.reform.hmc.helper.hmi.EntitySubTypeMapper.PERSON_CLASS_CODE;

class EntitySubTypeMapperTest {

    private static final String TITLE = "Title";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME = "LastName";
    private static final String INTERPRETER_LANGUAGE = "InterpreterLanguage";
    private static final String VULNERABLE = "Vulnerable";
    private static final String ORG_NAME = "OrgName";

    @Test
    void shouldReturnPersonEntitySubType() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle(TITLE);
        individualDetails.setFirstName(FIRST_NAME);
        individualDetails.setLastName(LAST_NAME);
        individualDetails.setInterpreterLanguage(INTERPRETER_LANGUAGE);
        individualDetails.setVulnerableFlag(true);
        individualDetails.setVulnerabilityDetails(VULNERABLE);
        EntitySubType expectedEntitySubType = EntitySubType.builder()
            .entityTitle(TITLE)
            .entityFirstName(FIRST_NAME)
            .entityLastName(LAST_NAME)
            .entityInterpreterLanguage(INTERPRETER_LANGUAGE)
            .entityClassCode(PERSON_CLASS_CODE)
            .entitySensitiveClient(true)
            .entityAlertMessage(VULNERABLE)
            .build();
        EntitySubTypeMapper entitySubTypeMapper = new EntitySubTypeMapper();
        EntitySubType actualEntitySubType = entitySubTypeMapper.getPersonEntitySubType(individualDetails);
        assertEquals(expectedEntitySubType, actualEntitySubType);
    }

    @Test
    void shouldReturnOrgEntitySubType() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName(ORG_NAME);
        EntitySubType expectedEntitySubType = EntitySubType.builder()
            .entityCompanyName(ORG_NAME)
            .entityClassCode(ORGANISATION_CLASS_CODE)
            .build();
        EntitySubTypeMapper entitySubTypeMapper = new EntitySubTypeMapper();
        EntitySubType actualEntitySubType = entitySubTypeMapper.getOrgEntitySubType(organisationDetails);
        assertEquals(expectedEntitySubType, actualEntitySubType);
    }
}
