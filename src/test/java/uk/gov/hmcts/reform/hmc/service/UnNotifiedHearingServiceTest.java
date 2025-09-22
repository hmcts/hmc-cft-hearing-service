package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.UnNotifiedHearingsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SERVICE_CODE_ABA1;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_STATUS_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;

public class UnNotifiedHearingServiceTest {

    @InjectMocks
    private UnNotifiedHearingServiceImpl unNotifiedHearingService;

    @Mock
    CaseHearingRequestRepository caseHearingRequestRepository;

    @Mock
    UnNotifiedHearingsRepository unNotifiedHearingsRepository;

    @Mock
    AccessControlService accessControlService;

    List<String> hearingStatus  = List.of("LISTED");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        unNotifiedHearingService =
            new UnNotifiedHearingServiceImpl(caseHearingRequestRepository,
                                             unNotifiedHearingsRepository,
                                             accessControlService);
    }

    @Nested
    @DisplayName("GetUnNotifiedHearings")
    class GetUnNotifiedHearings {
        @Test
        void shouldFailWithInvalidHmctsServiceCode() {
            LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);

            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings("AA", startFrom, null,
                                                               null));
            assertEquals(INVALID_HMCTS_SERVICE_CODE, exception.getMessage());
        }

        @Test
        void shouldFailWithNullHmctsServiceCode() {
            LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);

            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings(null, startFrom, null,
                                                               null));
            assertEquals(INVALID_HMCTS_SERVICE_CODE, exception.getMessage());
        }

        @Test
        void shouldFailWithEmptyHmctsServiceCode() {
            LocalDateTime startFrom = LocalDateTime.of(2019, 1, 10, 11, 20, 00);

            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings("", startFrom, null,
                                                               hearingStatus));
            assertEquals(INVALID_HMCTS_SERVICE_CODE, exception.getMessage());
        }

        @Test
        void shouldPassWithAllMandatoryDetails() {
            LocalDateTime dateTime = LocalDateTime.now();
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("TEST")).willReturn(1L);
            List<Long> hearingIds = Arrays.asList(2000000205L,2000000207L, 2000000206L);
            given(unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo("TEST", dateTime,
                                                                                   dateTime, hearingStatus
            )).willReturn(hearingIds);
            List<String> expectedHearingIds = Arrays.asList("2000000207","2000000206", "2000000205");
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("TEST", dateTime, dateTime, hearingStatus);
            assertThat(3).isEqualTo(response.getHearingIds().size());
            assertThat(3L).isEqualTo(response.getTotalFound());
            assertThat(response.getHearingIds()).containsExactlyInAnyOrderElementsOf(expectedHearingIds);
        }

        @Test
        void shouldPassWithOnlyStartDateFrom() {
            LocalDateTime dateTime = LocalDateTime.now();
            List<Long> hearingIds = Arrays.asList(2000000205L,2000000207L, 2000000206L);
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("TEST")).willReturn(1L);
            given(unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo("TEST", dateTime,
                                                                                       null
            )).willReturn(hearingIds);
            List<String> expectedHearingIds = Arrays.asList("2000000207","2000000206", "2000000205");
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("TEST", dateTime, null, null);
            assertThat(3).isEqualTo(response.getHearingIds().size());
            assertThat(3L).isEqualTo(response.getTotalFound());
            assertThat(response.getHearingIds()).containsExactlyInAnyOrderElementsOf(expectedHearingIds);
        }

        @Test
        void testWhenCriteriaDoesNotHaveDataForStartDateFrom() {
            LocalDateTime dateTime = LocalDateTime.now();
            List<Long> hearingIds = new ArrayList<>();
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("TEST")).willReturn(1L);
            given(unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo("TEST", dateTime,
                                                                                       hearingStatus
            )).willReturn(hearingIds);
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("TEST", dateTime, null, null);
            assertEquals(0, response.getHearingIds().size());
            assertEquals(0, response.getTotalFound());
        }


        @Test
        void testWhenCriteriaDoesNotHaveDataForStartDateTo() {
            LocalDateTime dateTime = LocalDateTime.now();
            List<Long> hearingIds = new ArrayList<>();
            given(caseHearingRequestRepository.getHmctsServiceCodeCount("TEST")).willReturn(1L);
            given(unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo("TEST", dateTime,
                                                                                   dateTime, hearingStatus
            )).willReturn(hearingIds);
            UnNotifiedHearingsResponse response = unNotifiedHearingService
                .getUnNotifiedHearings("TEST", dateTime, dateTime, hearingStatus);
            assertEquals(0, response.getHearingIds().size());
            assertEquals(0, response.getTotalFound());
        }

        @Test
        void testWhenHearingStatusHasExceptionOnly() {
            LocalDateTime dateTime = LocalDateTime.now();
            List<String> hearingStatus  = List.of("Exception");
            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings(SERVICE_CODE_ABA1, dateTime, dateTime,
                                                               hearingStatus));
            assertEquals(HEARING_STATUS_EXCEPTION, exception.getMessage());
        }

        @Test
        void testWhenHearingStatusHasExceptionAsOneParam() {
            LocalDateTime dateTime = LocalDateTime.now();
            List<String> hearingStatus  = List.of("Listed","Exception");
            Exception exception = assertThrows(BadRequestException.class, () ->
                unNotifiedHearingService.getUnNotifiedHearings(SERVICE_CODE_ABA1, dateTime, dateTime,
                                                               hearingStatus));
            assertEquals(HEARING_STATUS_EXCEPTION, exception.getMessage());
        }

    }
}
