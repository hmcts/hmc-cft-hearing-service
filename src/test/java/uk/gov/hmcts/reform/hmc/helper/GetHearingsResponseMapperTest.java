package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetHearingsResponseMapperTest {

    private static final Logger logger = LoggerFactory.getLogger(GetHearingsResponseMapperTest.class);

    public static final String VALID_CASE_REF = "9372710950276233";

    @Test
    void toHearingsResponseWhenDataIsPresent() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(VALID_CASE_REF, entities);
        assertEquals(VALID_CASE_REF, response.getCaseRef());
        assertEquals("TEST", response.getHmctsServiceCode());
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
            .getPanelMemberIds().get(0));
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
        assertTrue(response.getCaseHearings().get(0).getHearingChannels().contains("someChannelType"));
    }

    @Test
    void toHearingsResponseWhenDataIsPresentAndIsPresidingIsFalse() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        entities.get(0).getHearing().getHearingResponses().get(0).getHearingDayDetails().get(0)
            .getHearingDayPanel().get(0).setIsPresiding(false);
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(VALID_CASE_REF, entities);
        assertEquals(VALID_CASE_REF, response.getCaseRef());
        assertEquals("TEST", response.getHmctsServiceCode());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getPanelMemberIds().get(0));
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentAndIsPresidingIsTrue() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        entities.get(0).getHearing().getHearingResponses().get(0).getHearingDayDetails().get(0)
            .getHearingDayPanel().get(0).setIsPresiding(true);
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(VALID_CASE_REF, entities);
        assertEquals(VALID_CASE_REF, response.getCaseRef());
        assertEquals("TEST", response.getHmctsServiceCode());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getHearingJudgeId());
        assertEquals(0, response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getPanelMemberIds().size());
    }

    @Test
    void toHearingsResponseWhenDataIsPresentAndIsPresidingIsNull() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        entities.get(0).getHearing().getHearingResponses().get(0).getHearingDayDetails().get(0)
            .getHearingDayPanel().get(0).setIsPresiding(null);
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(VALID_CASE_REF, entities);
        assertEquals(VALID_CASE_REF, response.getCaseRef());
        assertEquals("TEST", response.getHmctsServiceCode());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals("PanelUser1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getPanelMemberIds().get(0));
        assertNull(response.getCaseHearings().get(0).getHearingDaySchedule().get(0).getHearingJudgeId());
    }

    @Test
    void toHearingsResponseWhenDataIsNotPresent() {
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(VALID_CASE_REF, new ArrayList<>());
        assertEquals(VALID_CASE_REF, response.getCaseRef());
        assertNull(response.getHmctsServiceCode());
        assertEquals(0, response.getCaseHearings().size());
    }

    @Test
    void toHearingsResponseWhenStatusIsHearingRequested() {
        List<CaseHearingRequestEntity> entities =
            Arrays.asList(TestingUtil.getCaseHearingsEntities("HEARING_REQUESTED"));
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(VALID_CASE_REF, entities);

        assertEquals("HEARING_REQUESTED", response.getCaseHearings().get(0).getHmcStatus());
    }

    @Test
    void toHearingsResponseWhenStatusIsListed() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities("LISTED"));
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(VALID_CASE_REF, entities);

        assertEquals("AWAITING_ACTUALS", response.getCaseHearings().get(0).getHmcStatus());
    }

    @Test
    void caseHearingsAndSchedulesAreSortedCorrectly() {
        List<CaseHearingRequestEntity> caseHearings = TestingUtil.createMultipleCaseHearingRequestEntities();
        String caseRef = "caseRef";

        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = getHearingsResponseMapper.toHearingsResponse(caseRef, caseHearings);

        List<CaseHearing> sortedCaseHearings = response.getCaseHearings();

        for (int i = 0; i < sortedCaseHearings.size() - 1; i++) {
            logger.info("hearing Ids {}:{} - {}:{})", i, (i + 1),
                        sortedCaseHearings.get(i).getHearingId(), sortedCaseHearings.get(i + 1).getHearingId());
            assertThat(sortedCaseHearings.get(i).getHearingId())
                .isGreaterThanOrEqualTo(sortedCaseHearings.get(i + 1).getHearingId());

            List<HearingDaySchedule> schedules = sortedCaseHearings.get(i).getHearingDaySchedule();
            if (null != schedules && schedules.size() > 0) {
                for (int j = 0; j < schedules.size() - 1; j++) {
                    logger.info(
                        "schedules {}:{} - {}:{})", j, j + 1,
                        schedules.get(j).getHearingStartDateTime(), schedules.get(j + 1).getHearingStartDateTime()
                    );
                    assertThat(schedules.get(j).getHearingStartDateTime()
                                   .isBefore(schedules.get(j + 1).getHearingStartDateTime())).isTrue();
                }
            }

            if (null != schedules) {
                List<Attendee> attendees = schedules.get(i).getAttendees();
                if (null != attendees && attendees.size() > 0) {
                    for (int k = 0; k < attendees.size() - 1; k++) {
                        logger.info(
                            "attendees {}:{} - {}:{})", k, k + 1,
                            attendees.get(k).getPartyId(), attendees.get(k + 1).getPartyId()
                        );
                        assertThat(attendees.get(k).getPartyId())
                                       .isLessThanOrEqualTo(attendees.get(k + 1).getPartyId());
                    }
                }
            }
        }
    }

}
