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
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetHearingsResponseMapperTest {

    final String validCaseRef = "9372710950276233";

    @Test
    void toHearingsResponseWhenDataIsPresent() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, entities);
        assertEquals(validCaseRef, response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceCode());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(true, response.getCaseHearings().get(0).getHearingIsLinkedFlag());
        assertEquals("1", response.getCaseHearings().get(0).getHearingGroupRequestId());
        assertEquals("venue1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getPanelMemberId().get(0));
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
        assertTrue(response.getCaseHearings().get(0).getHearingChannels().contains("someChannelType"));
    }

    @Test
    void toHearingsResponseWhenDataIsPresentAndIsPresidingIsFalse() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        entities.get(0).getHearing().getHearingResponses().get(0).getHearingDayDetails().get(0)
            .getHearingDayPanel().get(0).setIsPresiding(false);
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, entities);
        assertEquals(validCaseRef, response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceCode());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getPanelMemberId().get(0));
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentAndIsPresidingIsTrue() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        entities.get(0).getHearing().getHearingResponses().get(0).getHearingDayDetails().get(0)
            .getHearingDayPanel().get(0).setIsPresiding(true);
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, entities);
        assertEquals(validCaseRef, response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceCode());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getHearingJudgeId());
        assertEquals(0, response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getPanelMemberId().size());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentAndIsPresidingIsNull() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        entities.get(0).getHearing().getHearingResponses().get(0).getHearingDayDetails().get(0)
            .getHearingDayPanel().get(0).setIsPresiding(null);
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, entities);
        assertEquals(validCaseRef, response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceCode());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getPanelMemberId().get(0));
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
    }

    @Test
    void toHearingsResponseWhenDataIsNotPresent() {
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, new ArrayList<>());
        assertEquals(validCaseRef, response.getCaseRef());
        assertNull(response.getHmctsServiceCode());
        assertEquals(0, response.getCaseHearings().size());
    }

    @Test
    void toHearingsResponseWhenStatusIsHearingRequested() {
        List<CaseHearingRequestEntity> entities =
            Arrays.asList(TestingUtil.getCaseHearingsEntities("HEARING_REQUESTED"));
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, entities);

        assertEquals("HEARING_REQUESTED", response.getCaseHearings().get(0).getHmcStatus());
    }

    @Test
    void toHearingsResponseWhenStatusIsListed() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities("LISTED"));
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(validCaseRef, entities);

        assertEquals("AWAITING_ACTUALS", response.getCaseHearings().get(0).getHmcStatus());
    }
}
