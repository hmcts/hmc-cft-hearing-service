package uk.gov.hmcts.reform.hmc.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetHearingsResponseTest {

    @Test
    void caseHearingsAndSchedulesAreSortedCorrectly() {
        Attendee attendee1 = new Attendee();
        attendee1.setPartyId("2");

        Attendee attendee2 = new Attendee();
        attendee2.setPartyId("1");

        HearingDaySchedule schedule1 = new HearingDaySchedule();
        schedule1.setHearingStartDateTime(LocalDateTime.of(2023, 10, 1, 10, 0));
        schedule1.setAttendees(Arrays.asList(attendee1, attendee2));

        HearingDaySchedule schedule2 = new HearingDaySchedule();
        schedule2.setHearingStartDateTime(LocalDateTime.of(2023, 10, 1, 9, 0));
        schedule2.setAttendees(Arrays.asList(attendee2, attendee1));

        CaseHearing hearing1 = new CaseHearing();
        hearing1.setHearingId(2L);
        hearing1.setHearingDaySchedule(Arrays.asList(schedule1, schedule2));

        CaseHearing hearing2 = new CaseHearing();
        hearing2.setHearingId(1L);
        hearing2.setHearingDaySchedule(Arrays.asList(schedule2, schedule1));

        GetHearingsResponse response = new GetHearingsResponse();
        response.setCaseHearings(Arrays.asList(hearing1, hearing2));

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(sortedHearings.get(0).getHearingId()).isEqualTo(2L);
        assertThat(sortedHearings.get(1).getHearingId()).isEqualTo(1L);

        List<HearingDaySchedule> sortedSchedules1 = sortedHearings.get(0).getHearingDaySchedule();
        assertThat(sortedSchedules1.get(0).getHearingStartDateTime()).isEqualTo(LocalDateTime.of(2023, 10, 1, 9, 0));
        assertThat(sortedSchedules1.get(1).getHearingStartDateTime()).isEqualTo(LocalDateTime.of(2023, 10, 1, 10, 0));

        List<Attendee> sortedAttendees1 = sortedSchedules1.get(0).getAttendees();
        assertThat(sortedAttendees1.get(0).getPartyId()).isEqualTo("1");
        assertThat(sortedAttendees1.get(1).getPartyId()).isEqualTo("2");

        List<HearingDaySchedule> sortedSchedules2 = sortedHearings.get(1).getHearingDaySchedule();
        assertThat(sortedSchedules2.get(0).getHearingStartDateTime()).isEqualTo(LocalDateTime.of(2023, 10, 1, 9, 0));
        assertThat(sortedSchedules2.get(1).getHearingStartDateTime()).isEqualTo(LocalDateTime.of(2023, 10, 1, 10, 0));

        List<Attendee> sortedAttendees2 = sortedSchedules2.get(0).getAttendees();
        assertThat(sortedAttendees2.get(0).getPartyId()).isEqualTo("1");
        assertThat(sortedAttendees2.get(1).getPartyId()).isEqualTo("2");
    }

    @Test
    void getCaseHearingsReturnsEmptyListWhenCaseHearingsIsEmpty() {
        GetHearingsResponse response = new GetHearingsResponse();
        response.setCaseHearings(Collections.emptyList());

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(sortedHearings).isEmpty();
    }

    @Test
    void getCaseHearingsHandlesSingleElementList() {
        GetHearingsResponse response = new GetHearingsResponse();

        CaseHearing hearing = new CaseHearing();
        hearing.setHearingId(1L);

        response.setCaseHearings(Collections.singletonList(hearing));

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(sortedHearings).hasSize(1);
        assertThat(sortedHearings.get(0).getHearingId()).isEqualTo(1L);
    }

    @Test
    void getCaseHearingsHandlesDuplicateHearingIds() {
        GetHearingsResponse response = new GetHearingsResponse();

        CaseHearing hearing1 = new CaseHearing();
        hearing1.setHearingId(1L);

        CaseHearing hearing2 = new CaseHearing();
        hearing2.setHearingId(1L);

        response.setCaseHearings(Arrays.asList(hearing1, hearing2));

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(sortedHearings).hasSize(2);
        assertThat(sortedHearings.get(0).getHearingId()).isEqualTo(1L);
        assertThat(sortedHearings.get(1).getHearingId()).isEqualTo(1L);
    }

    @Test
    void getCaseHearingsReturnsSortedByHearingId() {
        CaseHearing hearing1 = new CaseHearing();
        hearing1.setHearingId(2L);

        CaseHearing hearing2 = new CaseHearing();
        hearing2.setHearingId(1L);

        CaseHearing hearing3 = new CaseHearing();
        hearing3.setHearingId(3L);

        GetHearingsResponse response = new GetHearingsResponse();
        response.setCaseHearings(Arrays.asList(hearing1, hearing2, hearing3));

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(hearing2).isEqualTo(sortedHearings.get(2));
        assertThat(hearing1).isEqualTo(sortedHearings.get(1));
        assertThat(hearing3).isEqualTo(sortedHearings.get(0));
    }

    @Test
    void getCaseHearingsReturnsEmptyListWhenEmpty() {
        GetHearingsResponse response = new GetHearingsResponse();
        response.setCaseHearings(Arrays.asList());

        List<CaseHearing> sortedHearings = response.getCaseHearings();

        assertThat(sortedHearings).isEmpty();
    }

}
