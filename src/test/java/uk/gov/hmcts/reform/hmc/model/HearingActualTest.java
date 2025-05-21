package uk.gov.hmcts.reform.hmc.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HearingActualTest {

    @Test
    void shouldReturnTrueWhenAllOutcomeFieldsAreNullOrEmpty() {
        HearingActualsOutcome outcome = new HearingActualsOutcome();
        assertTrue(outcome.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenHearingTypeIsNotEmpty() {
        HearingActualsOutcome outcome = new HearingActualsOutcome();
        outcome.setHearingType("Some Hearing Type");
        assertFalse(outcome.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenHearingFinalFlagIsNotNull() {
        HearingActualsOutcome outcome = new HearingActualsOutcome();
        outcome.setHearingFinalFlag(true);
        assertFalse(outcome.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenHearingResultIsNotNull() {
        HearingActualsOutcome outcome = new HearingActualsOutcome();
        outcome.setHearingResult("Result");
        assertFalse(outcome.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenHearingResultReasonTypeIsNotEmpty() {
        HearingActualsOutcome outcome = new HearingActualsOutcome();
        outcome.setHearingResultReasonType("Reason Type");
        assertFalse(outcome.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenHearingResultDateIsNotNull() {
        HearingActualsOutcome outcome = new HearingActualsOutcome();
        outcome.setHearingResultDate(LocalDate.now());
        assertFalse(outcome.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenAllHearingDayFieldsAreNullOrEmpty() {
        ActualHearingDay hearingDay = new ActualHearingDay();
        assertTrue(hearingDay.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenHearingStartTimeIsNotNull() {
        ActualHearingDay hearingDay = new ActualHearingDay();
        hearingDay.setHearingStartTime(LocalDateTime.now());
        assertFalse(hearingDay.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenHearingEndTimeIsNotNull() {
        ActualHearingDay hearingDay = new ActualHearingDay();
        hearingDay.setHearingEndTime(LocalDateTime.now());
        assertFalse(hearingDay.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenPauseDateTimesIsNotEmpty() {
        ActualHearingDay hearingDay = new ActualHearingDay();
        hearingDay.setPauseDateTimes(Collections.singletonList(new ActualHearingDayPauseDayTime()));
        assertFalse(hearingDay.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenActualDayPartiesIsNotEmpty() {
        ActualHearingDay hearingDay = new ActualHearingDay();
        hearingDay.setActualDayParties(Collections.singletonList(new ActualHearingDayParties()));
        assertFalse(hearingDay.isEmpty());
    }
}
