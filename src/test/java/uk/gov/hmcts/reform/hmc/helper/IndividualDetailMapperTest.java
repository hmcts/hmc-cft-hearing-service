package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IndividualDetailMapperTest {

    @Test
    void modelToEntity_RelatedPartyPresent() {
        IndividualDetailMapper mapper = new IndividualDetailMapper();
        IndividualDetails individualDetail = TestingUtil.individualDetails();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        IndividualDetailEntity entity = mapper.modelToEntity(individualDetail, hearingPartyEntity);
        assertNull(entity.getVulnerabilityDetails());
        assertNull(entity.getVulnerableFlag());
        assertNull(entity.getInterpreterLanguage());
        assertNull(entity.getChannelType());
        assertEquals("lastName", entity.getLastName());
        assertEquals("firstName", entity.getFirstName());
        assertEquals("custodyStatus", entity.getCustodyStatus());
        assertEquals("otherReason", entity.getOtherReasonableAdjustmentDetails());
        assertEquals("Mr", entity.getTitle());
    }

    @Test
    void modelToEntity_RelatedPartyNotPresent() {
        IndividualDetailMapper mapper = new IndividualDetailMapper();
        IndividualDetails individualDetail = individualDetails_RelatedPartyNotPresent();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        IndividualDetailEntity entity = mapper.modelToEntity(individualDetail, hearingPartyEntity);
        assertEquals("A bit vulnerable", entity.getVulnerabilityDetails());
        assertEquals(FALSE, entity.getVulnerableFlag());
        assertEquals("French", entity.getInterpreterLanguage());
        assertEquals("Email", entity.getChannelType());
        assertEquals("lastName", entity.getLastName());
        assertEquals("firstName", entity.getFirstName());
        assertEquals("Mr", entity.getTitle());
    }

    public static IndividualDetails individualDetails_RelatedPartyNotPresent() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Mr");
        individualDetails.setFirstName("firstName");
        individualDetails.setLastName("lastName");
        individualDetails.setVulnerabilityDetails("A bit vulnerable");
        individualDetails.setVulnerableFlag(false);
        individualDetails.setInterpreterLanguage("French");
        individualDetails.setPreferredHearingChannel("Email");
        return individualDetails;
    }

}
