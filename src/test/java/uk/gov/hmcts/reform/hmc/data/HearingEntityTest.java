package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HearingEntityTest {

    @Nested
    class GetLatestCaseHearingRequest {

        @Test
        void shouldGetLatestHearingRequest() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            CaseHearingRequestEntity caseHearingRequest3 = caseHearingRequest(3);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2, caseHearingRequest3));

            CaseHearingRequestEntity latestRequest = hearing.getLatestCaseHearingRequest();

            assertEquals(caseHearingRequest3, latestRequest);
        }

        @Test
        void shouldErrorWhenNoRequestsExistWithNullList() {
            HearingEntity hearing = new HearingEntity();
            hearing.setId(2000000001L);

            ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, hearing::getLatestCaseHearingRequest);
            assertEquals("Cannot find latest case hearing request for hearing 2000000001",
                         exception.getMessage());
        }

        @Test
        void shouldErrorWhenNoRequestsExistWithEmptyList() {
            HearingEntity hearing = new HearingEntity();
            hearing.setId(2000000001L);
            hearing.setHearingResponses(emptyList());

            ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, hearing::getLatestCaseHearingRequest);
            assertEquals("Cannot find latest case hearing request for hearing 2000000001",
                         exception.getMessage());
        }
    }

    @Nested
    class GetLatestRequestVersion {

        @Test
        void shouldGetLatestRequestVersion() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            CaseHearingRequestEntity caseHearingRequest3 = caseHearingRequest(3);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2, caseHearingRequest3));

            Integer latestRequestVersion = hearing.getLatestRequestVersion();

            assertEquals(3, latestRequestVersion);
        }

        @Test
        void shouldErrorWhenNoRequestsExistWithNullList() {
            HearingEntity hearing = new HearingEntity();
            hearing.setId(2000000002L);

            ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, hearing::getLatestRequestVersion);
            assertEquals("Cannot find latest case hearing request for hearing 2000000002",
                         exception.getMessage());
        }

        @Test
        void shouldErrorWhenNoRequestsExistWithEmptyList() {
            HearingEntity hearing = new HearingEntity();
            hearing.setId(2000000002L);
            hearing.setHearingResponses(emptyList());

            ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, hearing::getLatestRequestVersion);
            assertEquals("Cannot find latest case hearing request for hearing 2000000002",
                         exception.getMessage());
        }
    }

    @Nested
    class GetCaseHearingRequest {

        @Test
        void shouldGetSpecificRequestVersion() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            CaseHearingRequestEntity caseHearingRequest3 = caseHearingRequest(3);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2, caseHearingRequest3));

            CaseHearingRequestEntity result = hearing.getCaseHearingRequest(2);

            assertEquals(caseHearingRequest2, result);
        }

        @Test
        void shouldErrorWhenNoRequestsExist() {
            HearingEntity hearing = new HearingEntity();
            hearing.setId(2000000003L);

            ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> hearing.getCaseHearingRequest(1));
            assertEquals("Cannot find request version 1 for hearing 2000000003", exception.getMessage());
        }
    }

    @Nested
    class GetHearingResponseForLatestRequest {

        @Test
        void shouldGetLatestHearingResponseForLatestRequestWhenResponsesExist() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2));
            HearingResponseEntity hearingResponse1 = hearingResponse(1, 2000);
            HearingResponseEntity hearingResponse2 = hearingResponse(2, 2002);
            HearingResponseEntity hearingResponse3 = hearingResponse(2, 2004);
            hearing.setHearingResponses(List.of(hearingResponse1, hearingResponse2, hearingResponse3));

            Optional<HearingResponseEntity> latestResponse = hearing.getHearingResponseForLatestRequest();

            assertTrue(latestResponse.isPresent());
            assertEquals(hearingResponse3, latestResponse.get());
        }

        @Test
        void shouldGetLatestHearingResponseForLatestRequestForUpdateWhenResponsesExist() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2));
            HearingResponseEntity hearingResponse1 = hearingResponse(1, 2000);
            HearingResponseEntity hearingResponse2 = hearingResponse(2, 2002);
            HearingResponseEntity hearingResponse3 = hearingResponse(3, 2004);
            hearing.setHearingResponses(List.of(hearingResponse1, hearingResponse2, hearingResponse3));

            Optional<HearingResponseEntity> latestResponse = hearing.getHearingResponseForLatestRequestForUpdate();

            assertTrue(latestResponse.isPresent());
            assertEquals(hearingResponse3, latestResponse.get());
        }

        @Test
        void shouldGetLatestHearingResponseForLatestRequestForUpdateWhenNoResponsesExist() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2));
            Optional<HearingResponseEntity> latestResponse = hearing.getHearingResponseForLatestRequestForUpdate();
            assertTrue(latestResponse.isEmpty());
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoHearingResponsesExistForLatestRequestVersion() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2));
            HearingResponseEntity hearingResponse1 = hearingResponse(1, 2000);
            hearing.setHearingResponses(List.of(hearingResponse1));

            Optional<HearingResponseEntity> latestResponse = hearing.getHearingResponseForLatestRequest();

            assertTrue(latestResponse.isEmpty());
        }
    }

    @Nested
    class GetLatestHearingResponse {

        @Test
        void shouldGetLatestHearingResponse() {
            HearingEntity hearing = new HearingEntity();
            HearingResponseEntity hearingResponse1 = hearingResponse(1, 2000);
            HearingResponseEntity hearingResponse2 = hearingResponse(2, 2002);
            HearingResponseEntity hearingResponse3 = hearingResponse(2, 2004);
            hearing.setHearingResponses(List.of(hearingResponse1, hearingResponse2, hearingResponse3));

            Optional<HearingResponseEntity> latestResponse = hearing.getLatestHearingResponse();

            assertTrue(latestResponse.isPresent());
            assertEquals(hearingResponse3, latestResponse.get());
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoHearingResponsesExist() {
            HearingEntity hearing = new HearingEntity();

            Optional<HearingResponseEntity> latestResponse = hearing.getLatestHearingResponse();

            assertFalse(latestResponse.isPresent());
        }
    }

    @Nested
    class GetEarliestHearingDayDetails {

        @Test
        void shouldGetEarliestHearingDayDetailsIgnoringNullStartDates() {
            HearingDayDetailsEntity dayDetails1 = new HearingDayDetailsEntity();
            dayDetails1.setStartDateTime(LocalDateTime.of(2023, 1, 2, 10, 0));
            HearingDayDetailsEntity dayDetails2 = new HearingDayDetailsEntity();
            dayDetails2.setStartDateTime(null);
            HearingDayDetailsEntity dayDetails3 = new HearingDayDetailsEntity();
            dayDetails3.setStartDateTime(LocalDateTime.of(2023, 1, 1, 10, 0));
            HearingResponseEntity hearingResponse = new HearingResponseEntity();
            hearingResponse.setHearingDayDetails(List.of(dayDetails1, dayDetails2, dayDetails3));

            Optional<HearingDayDetailsEntity> earliest = hearingResponse.getEarliestHearingDayDetails();

            assertTrue(earliest.isPresent());
            assertEquals(dayDetails3.getStartDateTime(), earliest.get().getStartDateTime());
        }

        @Test
        void shouldReturnEmptyIfAllStartDatesAreNull() {
            HearingResponseEntity hearingResponse = new HearingResponseEntity();
            HearingDayDetailsEntity dayDetails1 = new HearingDayDetailsEntity();
            dayDetails1.setStartDateTime(null);
            HearingDayDetailsEntity dayDetails2 = new HearingDayDetailsEntity();
            dayDetails2.setStartDateTime(null);
            hearingResponse.setHearingDayDetails(List.of(dayDetails1, dayDetails2));

            Optional<HearingDayDetailsEntity> earliest = hearingResponse.getEarliestHearingDayDetails();

            assertFalse(earliest.isPresent());
        }
    }

    @Nested
    class GetDerivedHearingResponse {

        @Test
        void shouldGetDerivedHearingResponseWithStatusUpdated() {
            HearingEntity hearing = new HearingEntity();
            LocalDateTime startDateTime = LocalDateTime.of(2022, 4, 27, 10, 10);
            LocalDateTime endDateTime = LocalDateTime.of(2023, 5, 30, 11, 11);
            HearingResponseEntity hearingResponse = hearingResponseWithDayDetails(startDateTime, endDateTime);
            hearing.setHearingResponses(List.of(hearingResponse));
            hearing.setStatus("LISTED");

            String latestResponse = hearing.getDerivedHearingStatus();

            assertEquals("AWAITING_ACTUALS", latestResponse);
        }

        @Test
        void shouldGetDerivedHearingResponseWithStatusNotUpdated() {
            HearingEntity hearing = new HearingEntity();
            hearing.setStatus("HEARING_REQUESTED");

            String latestResponse = hearing.getDerivedHearingStatus();

            assertEquals("HEARING_REQUESTED", latestResponse);
        }

        @Test
        void shouldGetDefaultDerivedHearingResponseWithNotUpdated() {
            LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endDateTime = LocalDateTime.now().plusMonths(6);

            HearingResponseEntity hearingResponse =
                hearingResponseWithDayDetails(startDateTime, endDateTime);

            HearingEntity hearing = new HearingEntity();
            hearing.setHearingResponses(List.of(hearingResponse));
            hearing.setStatus("LISTED");

            String latestResponse = hearing.getDerivedHearingStatus();

            assertEquals("LISTED", latestResponse);
        }

        @Test
        void shouldGetDefaultDerivedHearingResponseWhenStartDateIsAheadOfToday() {
            LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endDateTime = LocalDateTime.now().plusMonths(6);

            HearingResponseEntity hearingResponse =
                hearingResponseWithDayDetails(startDateTime, endDateTime);
            HearingEntity hearing = new HearingEntity();
            hearing.setHearingResponses(List.of(hearingResponse));
            hearing.setStatus("LISTED");

            String latestResponse = hearing.getDerivedHearingStatus();
            assertEquals("LISTED", latestResponse);
        }

        @Test
        void shouldGetDerivedHearingAwaitingActualsWhenStartDateToday() {
            HearingEntity hearing = new HearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime endDateTime = LocalDateTime.now().plusDays(2);
            HearingResponseEntity hearingResponse = hearingResponseWithDayDetails(startDateTime, endDateTime);
            hearing.setHearingResponses(List.of(hearingResponse));
            hearing.setStatus("LISTED");

            String latestResponse = hearing.getDerivedHearingStatus();

            assertEquals("AWAITING_ACTUALS", latestResponse);
        }

        @Test
        void shouldGetDerivedHearingAwaitingActualsWhenStartDateYesterday() {
            HearingEntity hearing = new HearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now().minusDays(1);
            LocalDateTime endDateTime = LocalDateTime.now().plusDays(1);
            HearingResponseEntity hearingResponse = hearingResponseWithDayDetails(startDateTime, endDateTime);
            hearing.setHearingResponses(List.of(hearingResponse));
            hearing.setStatus("LISTED");

            String latestResponse = hearing.getDerivedHearingStatus();

            assertEquals("AWAITING_ACTUALS", latestResponse);
        }

        @Test
        void shouldGetDerivedHearingAwaitingActualsWhenStartDateTomorrow() {
            HearingEntity hearing = new HearingEntity();
            LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endDateTime = LocalDateTime.now().plusDays(3);
            HearingResponseEntity hearingResponse = hearingResponseWithDayDetails(startDateTime, endDateTime);
            hearing.setHearingResponses(List.of(hearingResponse));
            hearing.setStatus("LISTED");

            String latestResponse = hearing.getDerivedHearingStatus();

            assertEquals(hearing.getStatus(), latestResponse);
        }

    }

    private CaseHearingRequestEntity caseHearingRequest(int version) {
        CaseHearingRequestEntity caseHearingRequest = new CaseHearingRequestEntity();
        caseHearingRequest.setVersionNumber(version);
        return caseHearingRequest;
    }

    private HearingResponseEntity hearingResponse(int requestVersion, int timestampYear) {
        HearingResponseEntity hearingResponse = new HearingResponseEntity();
        hearingResponse.setRequestVersion(requestVersion);
        hearingResponse.setRequestTimeStamp(LocalDateTime.of(timestampYear, 1, 1, 12, 0));
        return hearingResponse;
    }

    private HearingResponseEntity hearingResponseWithDayDetails(LocalDateTime startDateTime,
                                                                LocalDateTime endDateTime) {
        HearingResponseEntity hearingResponse = new HearingResponseEntity();
        hearingResponse.setRequestVersion(1);
        hearingResponse.setRequestTimeStamp(LocalDateTime.of(2000, 1, 1, 12, 0));

        HearingDayDetailsEntity hearingDayDetailsEntity = new HearingDayDetailsEntity();
        hearingDayDetailsEntity.setStartDateTime(startDateTime);
        hearingDayDetailsEntity.setEndDateTime(endDateTime);
        hearingResponse.setHearingDayDetails(List.of(hearingDayDetailsEntity));

        return hearingResponse;
    }
}
