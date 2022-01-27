package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ListingJohsMapperTest {

    private static final String MEMBER_ID = "MemberId";
    private static final String MEMBER_ID_TWO = "MemberIdTwo";
    private static final String REQUIREMENT_TYPE = "RequirementType";
    private static final String REQUIREMENT_TYPE_TWO = "RequirementTypeTwo";

    @Test
    void shouldReturnListingJohs() {
        PanelPreference panelPreference = new PanelPreference();
        panelPreference.setMemberID(MEMBER_ID);
        panelPreference.setRequirementType(REQUIREMENT_TYPE);
        PanelPreference panelPreferenceTwo = new PanelPreference();
        panelPreferenceTwo.setMemberID(MEMBER_ID_TWO);
        panelPreferenceTwo.setRequirementType(REQUIREMENT_TYPE_TWO);
        PanelRequirements panelRequirements = new PanelRequirements();
        List<PanelPreference> panelPreferences = Arrays.asList(panelPreference, panelPreferenceTwo);
        panelRequirements.setPanelPreferences(panelPreferences);
        ListingJoh listingJoh = ListingJoh.builder()
            .listingJohId(MEMBER_ID)
            .listingJohPreference(REQUIREMENT_TYPE)
            .build();
        ListingJoh listingJohTwo = ListingJoh.builder()
            .listingJohId(MEMBER_ID_TWO)
            .listingJohPreference(REQUIREMENT_TYPE_TWO)
            .build();
        List<ListingJoh> expectedListingJohs = Arrays.asList(listingJoh, listingJohTwo);
        ListingJohsMapper listingJohsMapper = new ListingJohsMapper();
        List<ListingJoh> actualListingJohs = listingJohsMapper.getListingJohs(panelRequirements);
        assertEquals(expectedListingJohs, actualListingJohs);
    }

    @Test
    void shouldHandleNullPanelRequirements() {
        ListingJohsMapper listingJohsMapper = new ListingJohsMapper();
        List<ListingJoh> listingJohs = listingJohsMapper.getListingJohs(null);
        assertTrue(listingJohs.isEmpty());
    }

    @Test
    void shouldHandleNullPanelPreferences() {
        ListingJohsMapper listingJohsMapper = new ListingJohsMapper();
        PanelRequirements panelRequirements = new PanelRequirements();
        List<ListingJoh> listingJohs = listingJohsMapper.getListingJohs(panelRequirements);
        assertTrue(listingJohs.isEmpty());
    }
}
