package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        void shouldErrorWhenNoRequestsExist() {
            HearingEntity hearing = new HearingEntity();

            assertThrows(ResourceNotFoundException.class, hearing::getLatestCaseHearingRequest);
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
        void shouldErrorWhenNoRequestsExist() {
            HearingEntity hearing = new HearingEntity();

            assertThrows(ResourceNotFoundException.class, hearing::getLatestRequestVersion);
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
            HearingResponseEntity hearingResponse1 = hearingResponse("1", 2000);
            HearingResponseEntity hearingResponse2 = hearingResponse("2", 2002);
            HearingResponseEntity hearingResponse3 = hearingResponse("2", 2004);
            hearing.setHearingResponses(List.of(hearingResponse1, hearingResponse2, hearingResponse3));

            Optional<HearingResponseEntity> latestResponse = hearing.getHearingResponseForLatestRequest();

            assertTrue(latestResponse.isPresent());
            assertEquals(hearingResponse3, latestResponse.get());
        }

        @Test
        void shouldReturnEmptyOptionalWhenNoHearingResponsesExistForLatestRequestVersion() {
            HearingEntity hearing = new HearingEntity();
            CaseHearingRequestEntity caseHearingRequest1 = caseHearingRequest(1);
            CaseHearingRequestEntity caseHearingRequest2 = caseHearingRequest(2);
            hearing.setCaseHearingRequests(List.of(caseHearingRequest1, caseHearingRequest2));
            HearingResponseEntity hearingResponse1 = hearingResponse("1", 2000);
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
            HearingResponseEntity hearingResponse1 = hearingResponse("1", 2000);
            HearingResponseEntity hearingResponse2 = hearingResponse("2", 2002);
            HearingResponseEntity hearingResponse3 = hearingResponse("2", 2004);
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

    private CaseHearingRequestEntity caseHearingRequest(int version) {
        CaseHearingRequestEntity caseHearingRequest = new CaseHearingRequestEntity();
        caseHearingRequest.setVersionNumber(version);
        return caseHearingRequest;
    }

    private HearingResponseEntity hearingResponse(String requestVersion, int timestampYear) {
        HearingResponseEntity hearingResponse = new HearingResponseEntity();
        hearingResponse.setRequestVersion(requestVersion);
        hearingResponse.setRequestTimeStamp(LocalDateTime.of(timestampYear, 1, 1, 12, 0));
        return hearingResponse;
    }
}
