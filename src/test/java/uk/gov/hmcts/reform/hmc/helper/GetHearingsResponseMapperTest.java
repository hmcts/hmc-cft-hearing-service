package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GetHearingsResponseMapperTest {

    final String validCaseRef = "9372710950276233";

    @Test
    void toHearingsResponseWhenDataIsPresent() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, entities);
        assertEquals(validCaseRef, response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceId());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getPanelMemberId());
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
    }

    @Test
    void toHearingsResponseWhenDataIsNotPresent() {
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, new ArrayList<>());
        assertEquals(validCaseRef, response.getCaseRef());
        assertNull(response.getHmctsServiceId());
        assertEquals(0, response.getCaseHearings().size());
    }
}
