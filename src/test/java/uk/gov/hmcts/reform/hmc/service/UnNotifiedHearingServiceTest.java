package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FIRST_PAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;

public class UnNotifiedHearingServiceTest {

    @InjectMocks
    private UnNotifiedHearingServiceImpl unNotifiedHearingService;

    @Mock
    CaseHearingRequestRepository caseHearingRequestRepository;

    @Mock
    HearingResponseRepository hearingResponseRepository;

    @Mock
    AccessControlService accessControlService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        unNotifiedHearingService =
            new UnNotifiedHearingServiceImpl(caseHearingRequestRepository,
                                             hearingResponseRepository,
                                             accessControlService);
    }

    @Nested
    @DisplayName("GetUnNotifiedHearings")
    class GetUnNotifiedHearings {
        @Test
        void shouldFailWithInvalidHmctsServiceCode() {
            LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);

            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings("AA", startFrom, null));
            assertEquals(INVALID_HMCTS_SERVICE_CODE, exception.getMessage());
        }

        @Test
        void shouldFailWithNullHmctsServiceCode() {
            LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);

            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings(null, startFrom, null));
            assertEquals(INVALID_HMCTS_SERVICE_CODE, exception.getMessage());
        }

        @Test
        void shouldFailWithEmptyHmctsServiceCode() {
            LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);

            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings("", startFrom, null));
            assertEquals(INVALID_HMCTS_SERVICE_CODE, exception.getMessage());
        }

        @Test
        void shouldPassWithAllMandatoryDetails() {
            LocalDateTime dateTime = LocalDateTime.now();
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("ABA1")).willReturn(1L);
            PageRequest limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
            List<Long> hearingIds = Arrays.asList(2000000207L, 2000000206L, 2000000205L);
            Page<Long> unNotifiedHearingsData = new PageImpl<>(hearingIds, limit, hearingIds.size());
            given(hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo("ABA1", dateTime,
                                                                                   dateTime, limit
            )).willReturn(unNotifiedHearingsData);
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("ABA1", dateTime, dateTime);
            assertEquals(3, response.getHearingIds().size());
            assertEquals(3, response.getTotalFound());
        }

        @Test
        void shouldPassWithOnlyStartDateFrom() {
            LocalDateTime dateTime = LocalDateTime.now();
            Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
            List<Long> hearingIds = Arrays.asList(2000000207L, 2000000206L, 2000000205L);
            Page<Long> unNotifiedHearingsData = new PageImpl<>(hearingIds, limit, hearingIds.size());
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("ABA1")).willReturn(1L);
            given(hearingResponseRepository.getUnNotifiedHearingsWithOutStartDateTo("ABA1", dateTime,
                                                                                   limit
            )).willReturn(unNotifiedHearingsData);
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("ABA1", dateTime, null);
            assertEquals(3, response.getHearingIds().size());
            assertEquals(3, response.getTotalFound());
        }

        @Test
        void testWhenCriteriaDoesNotHaveDataForStartDateFrom() {
            LocalDateTime dateTime = LocalDateTime.now();
            Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
            List<Long> hearingIds = new ArrayList<>();
            Page<Long> unNotifiedHearingsData = new PageImpl<>(hearingIds, limit, hearingIds.size());
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("ABA1")).willReturn(1L);
            given(hearingResponseRepository.getUnNotifiedHearingsWithOutStartDateTo("ABA1", dateTime,
                                                                                    limit
            )).willReturn(unNotifiedHearingsData);
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("ABA1", dateTime, null);
            assertEquals(0, response.getHearingIds().size());
            assertEquals(0, response.getTotalFound());
        }


        @Test
        void testWhenCriteriaDoesNotHaveDataForStartDateTo() {
            LocalDateTime dateTime = LocalDateTime.now();
            Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
            List<Long> hearingIds = new ArrayList<>();
            Page<Long> unNotifiedHearingsData = new PageImpl<>(hearingIds, limit, hearingIds.size());
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("ABA1")).willReturn(1L);
            given(hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo("ABA1", dateTime,
                                                                                   dateTime, limit
            )).willReturn(unNotifiedHearingsData);
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("ABA1", dateTime, dateTime);
            assertEquals(0, response.getHearingIds().size());
            assertEquals(0, response.getTotalFound());
        }

    }
}
