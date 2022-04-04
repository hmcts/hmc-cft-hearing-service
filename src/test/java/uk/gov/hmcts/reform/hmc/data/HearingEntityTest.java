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
}
