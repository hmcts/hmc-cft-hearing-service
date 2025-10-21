package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;
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

        List<Long> hearingIds = new ArrayList<>();

        for (CaseHearing sortedCaseHearing : sortedCaseHearings) {
            hearingIds.add(sortedCaseHearing.getHearingId());

            List<HearingDaySchedule> schedules = sortedCaseHearing.getHearingDaySchedule();
            List<LocalDateTime> startDateTimes = new ArrayList<>();

            if (null != schedules && !schedules.isEmpty()) {
                for (HearingDaySchedule schedule : schedules) {
                    startDateTimes.add(schedule.getHearingStartDateTime());

                    List<Attendee> attendees = schedule.getAttendees();
                    List<String> partyIds = new ArrayList<>();
                    if (null != attendees && !attendees.isEmpty()) {
                        for (Attendee attendee : attendees) {
                            partyIds.add(attendee.getPartyId());
                        }
                    }
                    assertThat(isAscendingOrder("partyId", partyIds)).isTrue();
                }
                assertThat(isAscendingLocalDateTimeOrder("startDateTime", startDateTimes)).isTrue();
            }
        }
        assertThat(isDescendingOrder("hearingId", hearingIds)).isTrue();
    }

    @Test
    void caseHearingsAndSchedulesAreSortedCorrectlyWhenPartyIdIsNull() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        HearingDayDetailsEntity detailEntity = entities.get(0).getHearing().getHearingResponses().get(0)
            .getHearingDayDetails().get(0);
        HearingAttendeeDetailsEntity attendeeDetailsEntity = entities.get(0).getHearing().getHearingResponses().get(0)
            .getHearingDayDetails().get(0).getHearingAttendeeDetails().get(0);
        HearingAttendeeDetailsEntity entity = new HearingAttendeeDetailsEntity();
        entity.setPartySubChannelType("SubChannel1");
        entity.setPartyId(null);
        detailEntity.setHearingAttendeeDetails(Arrays.asList(entity, attendeeDetailsEntity));

        GetHearingsResponse response = new GetHearingsResponseMapper().toHearingsResponse(VALID_CASE_REF, entities);
        assertCommonResponseFields(response, VALID_CASE_REF, "TEST", 1);
        CaseHearing ch = response.getCaseHearings().get(0);
        assertThat(ch.getHearingDaySchedule().get(0).getAttendees().get(0).getHearingSubChannel())
            .isEqualTo("SubChannel1");
        assertThat(ch.getHearingDaySchedule().get(0).getPanelMemberIds().get(0)).isEqualTo("PanelUser1");
        assertNull(ch.getHearingDaySchedule().get(0).getHearingJudgeId());
        assertThat(ch.getHearingChannels()).contains("someChannelType");
    }

    @Test
    void caseHearingsAndSchedulesAreSortedCorrectlyWhenPartyIdsAreNull() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        HearingAttendeeDetailsEntity attendeeDetailsEntity = entities.get(0).getHearing().getHearingResponses().get(0)
            .getHearingDayDetails().get(0).getHearingAttendeeDetails().get(0);
        attendeeDetailsEntity.setPartyId(null);

        HearingAttendeeDetailsEntity entity = new HearingAttendeeDetailsEntity();
        entity.setPartySubChannelType("SubChannel1");
        entity.setPartyId(null);
        HearingDayDetailsEntity detailEntity = entities.get(0).getHearing().getHearingResponses().get(0)
            .getHearingDayDetails().get(0);
        detailEntity.setHearingAttendeeDetails(Arrays.asList(entity,attendeeDetailsEntity));

        GetHearingsResponse response = new GetHearingsResponseMapper().toHearingsResponse(VALID_CASE_REF, entities);
        assertCommonResponseFields(response, VALID_CASE_REF, "TEST", 1);
        CaseHearing ch = response.getCaseHearings().get(0);
        assertThat(ch.getHearingDaySchedule().get(0).getAttendees().get(0).getHearingSubChannel())
            .isEqualTo("SubChannel1");
        assertThat(ch.getHearingDaySchedule().get(0).getPanelMemberIds().get(0)).isEqualTo("PanelUser1");
        assertNull(ch.getHearingDaySchedule().get(0).getHearingJudgeId());
        assertThat(ch.getHearingChannels()).contains("someChannelType");
    }

    @Test
    void caseHearingsAndSchedulesAreSortedCorrectlyWhenAttendeeDetailsAreNull() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        HearingDayDetailsEntity detailEntity = entities.get(0).getHearing().getHearingResponses().get(0)
            .getHearingDayDetails().get(0);
        detailEntity.setHearingAttendeeDetails(null);
        GetHearingsResponseMapper getHearingsResponseMapper = new GetHearingsResponseMapper();
        GetHearingsResponse response = new GetHearingsResponseMapper().toHearingsResponse(VALID_CASE_REF, entities);
        assertCommonResponseFields(response, VALID_CASE_REF, "TEST", 1);
        CaseHearing ch = response.getCaseHearings().get(0);
        assertThat(ch.getHearingDaySchedule().get(0).getPanelMemberIds().get(0)).isEqualTo("PanelUser1");
        assertThat(ch.getHearingDaySchedule().get(0).getHearingJudgeId()).isNull();
        assertThat(ch.getHearingChannels()).contains("someChannelType");
    }

    private static <T extends Comparable<T>> boolean isAscendingOrder(String listName, List<T> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            logger.debug("{} {}:{} - {}:{})", listName, i, (i + 1), list.get(i), list.get(i + 1));
            if (list.get(i).compareTo(list.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAscendingLocalDateTimeOrder(String listName, List<LocalDateTime> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            logger.debug("{} {}:{} - {}:{})", listName, i, (i + 1), list.get(i), list.get(i + 1));
            if (list.get(i).isAfter(list.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    private static <T extends Comparable<T>> boolean isDescendingOrder(String listName, List<T> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            logger.debug("{} {}:{} - {}:{})", listName, i, (i + 1), list.get(i), list.get(i + 1));
            if (list.get(i).compareTo(list.get(i + 1)) < 0) {
                return false;
            }
        }
        return true;
    }

    private static void assertCommonResponseFields(GetHearingsResponse response, String expectedCaseRef,
                                                   String expectedServiceCode, int expectedCaseHearings) {
        assertEquals(expectedCaseRef, response.getCaseRef());
        if (expectedServiceCode == null) {
            assertNull(response.getHmctsServiceCode());
        } else {
            assertEquals(expectedServiceCode, response.getHmctsServiceCode());
        }
        assertEquals(expectedCaseHearings, response.getCaseHearings().size());
    }

}
