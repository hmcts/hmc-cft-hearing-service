package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingDayDetailsEntity;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingResponseEntity;

@ExtendWith(MockitoExtension.class)
class HearingHelperTest {

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private HearingHelper hearingHelper;

    @BeforeEach
    public void setUp() {
        hearingHelper = new HearingHelper(hearingRepository);
    }

    @Test
    void shouldGetLowestStartDateOfMostRecentHearingResponse() {

        HearingDayDetailsEntity day1 = hearingDayDetailsEntity(LocalDateTime.parse("2020-08-13T12:20:00"));
        HearingDayDetailsEntity day2 = hearingDayDetailsEntity(LocalDateTime.parse("2020-08-14T12:30:00"));
        List<HearingDayDetailsEntity> hearingDayDetails = List.of(day1, day2);
        HearingResponseEntity hearingResponse1 = hearingResponseEntity(1, 2,
                                                                       LocalDateTime.parse("2020-08-12T12:20:00"),
                                                                       hearingDayDetails);
        HearingResponseEntity hearingResponse2 = hearingResponseEntity(2, 2,
                                                                       LocalDateTime.parse("2020-08-11T12:20:00"),
                                                                       List.of());
        HearingResponseEntity hearingResponse3 = hearingResponseEntity(3, 1,
                                                                       LocalDateTime.parse("2020-08-10T12:20:00"),
                                                                       List.of());
        HearingEntity hearing = new HearingEntity();
        hearing.setHearingResponses(List.of(hearingResponse1, hearingResponse2, hearingResponse3));

        LocalDateTime lowestStartDateOfMostRecentHearingResponse = hearingHelper
            .getLowestStartDateOfMostRecentHearingResponse(hearing);

        assertEquals(LocalDateTime.of(LocalDate.of(2020, 8, 13), LocalTime.of(12, 20)),
                     lowestStartDateOfMostRecentHearingResponse);
    }

    @Test
    void shouldThrowErrorWhenNoHearingResponseFound() {
        HearingEntity hearing = new HearingEntity();
        hearing.setHearingResponses(List.of());

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingHelper.getLowestStartDateOfMostRecentHearingResponse(hearing);
        });
        assertEquals("Invalid hearing state", exception.getMessage());
    }

    @Test
    void shouldThrowErrorWhenNoHearingDayDetailsFound() {
        List<HearingDayDetailsEntity> hearingDayDetails = List.of();
        HearingResponseEntity hearingResponse1 = hearingResponseEntity(1, 2,
                                                                       LocalDateTime.parse("2020-08-12T12:20:00"),
                                                                       hearingDayDetails);
        HearingResponseEntity hearingResponse2 = hearingResponseEntity(3, 1,
                                                                       LocalDateTime.parse("2020-08-10T12:20:00"),
                                                                       List.of());
        HearingEntity hearing = new HearingEntity();
        hearing.setHearingResponses(List.of(hearingResponse1, hearingResponse2));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingHelper.getLowestStartDateOfMostRecentHearingResponse(hearing);
        });
        assertEquals("Invalid hearing state", exception.getMessage());
    }

}
