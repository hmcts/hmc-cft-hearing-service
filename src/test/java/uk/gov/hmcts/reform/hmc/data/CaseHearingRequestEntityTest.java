package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;
import uk.gov.hmcts.reform.hmc.model.LocationType;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RequirementType;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.model.CaseCategoryType.CASETYPE;
import static uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType.ALL;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingChannelsEntity;

class CaseHearingRequestEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            CaseHearingRequestEntity caseHearingRequest1 = TestingUtil.caseHearingRequestEntityWithPartyOrgForClone();
            CaseHearingRequestEntity response = new CaseHearingRequestEntity(caseHearingRequest1);
            assertEquals(response.getHearingParties().get(0).getPartyReference(),
                         TestingUtil.caseHearingRequestEntityWithPartyOrg()
                             .getHearingParties().get(0).getPartyReference());
            assertEquals(response.getHearingParties().get(0).getPartyRoleType(),
                         TestingUtil.caseHearingRequestEntityWithPartyOrg()
                             .getHearingParties().get(0).getPartyRoleType());
            assertEquals(response.getHearingParties().get(0).getPartyType(),
                         TestingUtil.caseHearingRequestEntityWithPartyOrg().getHearingParties().get(0).getPartyType());
            assertEquals(1, response.getCaseCategories().size());
            assertEquals(1, response.getHearingParties().size());
            assertEquals(1, response.getNonStandardDurations().size());
            assertEquals(1, response.getRequiredFacilities().size());
            assertEquals(1, response.getRequiredLocations().size());
            assertEquals(1, response.getPanelAuthorisationRequirements().size());
            assertEquals(1, response.getPanelRequirements().size());
            assertEquals(1, response.getPanelUserRequirements().size());
        }
    }

    @Test
    void testClone() throws CloneNotSupportedException {
        CaseHearingRequestEntity entity = TestingUtil.caseHearingRequestEntityWithPartyOrgForClone();
        entity.setHearingChannels(hearingChannelsEntity());
        CaseHearingRequestEntity cloned = (CaseHearingRequestEntity) entity.clone();
        assert1(cloned);
        assert2(cloned);
        assert3(cloned);
    }

    private void assert3(CaseHearingRequestEntity cloned) {
        assertEquals("RoleType1", cloned.getPanelRequirements().get(0).getRoleType());
        assertEquals(1, cloned.getPanelAuthorisationRequirements().size());
        assertEquals("AuthorisationType1", cloned.getPanelAuthorisationRequirements()
            .get(0).getAuthorisationType());
        assertEquals("AuthorisationSubType2", cloned.getPanelAuthorisationRequirements()
            .get(0).getAuthorisationSubType());
        assertEquals(1, cloned.getPanelSpecialisms().size());
        assertEquals("Specialism 1", cloned.getPanelSpecialisms().get(0).getSpecialismType());
        assertEquals(1, cloned.getPanelUserRequirements().size());
        assertEquals("judge1", cloned.getPanelUserRequirements().get(0).getJudicialUserId());
        assertEquals(RequirementType.MUSTINC, cloned.getPanelUserRequirements().get(0).getRequirementType());
        assertEquals("Type 1", cloned.getPanelUserRequirements().get(0).getUserType());
        assertEquals("someChannelType", cloned.getHearingChannels().get(0).getHearingChannelType());
        assertEquals("someOtherChannelType", cloned.getHearingChannels().get(1).getHearingChannelType());
    }

    private void assert2(CaseHearingRequestEntity cloned) {
        assertEquals("Some hearing type", cloned.getHearingType());
        assertEquals("ABA1", cloned.getHmctsServiceCode());
        assertEquals("12345", cloned.getCaseReference());
        assertEquals(1, cloned.getNonStandardDurations().size());
        assertEquals("Reason", cloned.getNonStandardDurations().get(0)
            .getNonStandardHearingDurationReasonType());
        assertEquals(1, cloned.getRequiredLocations().size());
        assertEquals(LocationType.COURT, cloned.getRequiredLocations().get(0).getLocationLevelType());
        assertEquals(1, cloned.getRequiredFacilities().size());
        assertEquals("string", cloned.getRequiredFacilities().get(0).getFacilityType());
        assertEquals(1, cloned.getCaseCategories().size());
        assertEquals(CASETYPE, cloned.getCaseCategories().get(0).getCategoryType());
        assertEquals("PROBATE", cloned.getCaseCategories().get(0).getCaseCategoryValue());
        assertEquals(1, cloned.getPanelRequirements().size());
    }

    private void assert1(CaseHearingRequestEntity cloned) {
        assertEquals(1, cloned.getHearingParties().size());
        assertEquals("reference", cloned.getHearingParties().get(0).getPartyReference());
        assertEquals(PartyType.ORG, cloned.getHearingParties().get(0).getPartyType());
        assertEquals("role", cloned.getHearingParties().get(0).getPartyRoleType());
        assertEquals("name", cloned.getHearingParties().get(0)
            .getOrganisationDetailEntity().getOrganisationName());
        assertEquals("code", cloned.getHearingParties().get(0)
            .getOrganisationDetailEntity().getOrganisationTypeCode());
        assertEquals("reference", cloned.getHearingParties().get(0)
            .getOrganisationDetailEntity().getHmctsOrganisationReference());
        assertEquals(ALL, cloned.getHearingParties().get(0)
            .getUnavailabilityEntity().get(0).getDayOfWeekUnavailableType());
        assertEquals(DayOfWeekUnavailable.FRIDAY, cloned.getHearingParties().get(0)
            .getUnavailabilityEntity().get(0).getDayOfWeekUnavailable());
        assertEquals("DOW", cloned.getHearingParties().get(0)
            .getUnavailabilityEntity().get(0).getUnAvailabilityType());
        assertEquals("Range", cloned.getHearingParties().get(0)
            .getUnavailabilityEntity().get(1).getUnAvailabilityType());
    }

}
