package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IndividualDetailMapperTest {

    @Test
    void modelToEntity_RelatedPartyPresent() {
        IndividualDetailMapper mapper = new IndividualDetailMapper();
        IndividualDetails individualDetail = TestingUtil.individualDetails();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<IndividualDetailEntity> entities = mapper.modelToEntity(individualDetail, hearingPartyEntity);
        assertNull(entities.get(0).getVulnerabilityDetails());
        assertNull(entities.get(0).getVulnerableFlag());
        assertNull(entities.get(0).getInterpreterLanguage());
        assertNull(entities.get(0).getChannelType());
        assertEquals("lastName", entities.get(0).getLastName());
        assertEquals("firstName", entities.get(0).getFirstName());
        assertEquals("custodyStatus", entities.get(0).getCustodyStatus());
        assertEquals("otherReason", entities.get(0).getOtherReasonableAdjustmentDetails());
        assertEquals("Mr", entities.get(0).getTitle());
    }

    @Test
    void modelToEntity_RelatedPartyNotPresent() {
        IndividualDetailMapper mapper = new IndividualDetailMapper();
        IndividualDetails individualDetail = individualDetails_RelatedPartyNotPresent();
        HearingPartyEntity hearingPartyEntity = new HearingPartyEntity();
        List<IndividualDetailEntity> entities = mapper.modelToEntity(individualDetail, hearingPartyEntity);
        assertEquals("A bit vulnerable", entities.get(0).getVulnerabilityDetails());
        assertEquals(FALSE, entities.get(0).getVulnerableFlag());
        assertEquals("French", entities.get(0).getInterpreterLanguage());
        assertEquals("Email", entities.get(0).getChannelType());
        assertEquals("lastName", entities.get(0).getLastName());
        assertEquals("firstName", entities.get(0).getFirstName());
        assertEquals("Mr", entities.get(0).getTitle());
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
