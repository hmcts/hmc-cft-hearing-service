package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CaseHearingRequestRepositoryTest {

    @Mock
    private CaseHearingRequestRepository caseHearingRequestRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetVersionNumber() {
        Integer expectedVersionNumber = 1;
        doReturn(1).when(caseHearingRequestRepository).getLatestVersionNumber(any());
        Integer versionNumber = caseHearingRequestRepository.getLatestVersionNumber(any());
        assertAll(
            () -> assertThat(versionNumber, is(expectedVersionNumber)),
            () -> verify(caseHearingRequestRepository, times(1)).getLatestVersionNumber(any())
        );
    }

    @Test
    void testGetVersionNumber_IsInvalid() {
        Integer expectedVersionNumber = 1;
        doReturn(2).when(caseHearingRequestRepository).getLatestVersionNumber(any());
        Integer versionNumber = caseHearingRequestRepository.getLatestVersionNumber(any());
        assertAll(
            () -> assertNotEquals(versionNumber, expectedVersionNumber),
            () -> verify(caseHearingRequestRepository, times(1)).getLatestVersionNumber(any())
        );
    }


    @Test
    void testGetHearingDetailsWhenStatusPresent() {
        List<CaseHearingRequestEntity> expectedDetails = TestingUtil.getCaseHearingsEntitiesWithStatus();
        doReturn(expectedDetails).when(caseHearingRequestRepository).getHearingDetailsWithStatus(any(), any());
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository.getHearingDetailsWithStatus(
            any(),
            any()
        );
        assertAll(
            () -> assertEquals(2, entities.size()),
            () -> assertEquals("12345", entities.get(0).getCaseReference()),
            () -> assertEquals("ABA1", entities.get(0).getHmctsServiceCode()),
            () -> assertEquals("HEARING_REQUESTED", entities.get(0).getHearing().getStatus()),
            () -> assertEquals(2000000000L, entities.get(0).getHearing().getId()),
            () -> assertEquals(1, entities.get(0).getHearing().getHearingResponses().size()),
            () -> assertEquals("4567", entities.get(1).getCaseReference()),
            () -> assertEquals("ABA1", entities.get(1).getHmctsServiceCode()),
            () -> assertEquals("HEARING_UPDATED", entities.get(1).getHearing().getStatus()),
            () -> assertEquals(2000000001L, entities.get(1).getHearing().getId()),
            () -> verify(caseHearingRequestRepository, times(1))
                .getHearingDetailsWithStatus(any(), any())
        );
    }

    @Test
    void testGetHearingDetailsWhenStatusNotPresent() {
        List<CaseHearingRequestEntity> expectedDetails = List.of(TestingUtil.getCaseHearingsEntities());
        doReturn(expectedDetails).when(caseHearingRequestRepository).getHearingDetails(any());
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository.getHearingDetails(any());
        assertAll(
            () -> assertEquals("12345", entities.get(0).getCaseReference()),
            () -> assertEquals("ABA1", entities.get(0).getHmctsServiceCode()),
            () -> assertEquals(2000000000L, entities.get(0).getHearing().getId()),
            () -> assertEquals(1, entities.get(0).getHearing().getHearingResponses().size()),
            () -> assertTrue(entities.get(0).getHearing().getIsLinkedFlag()),
            () -> verify(caseHearingRequestRepository, times(1)).getHearingDetails(any())
        );
    }

    @Test
    void testGetCaseHearingId() {
        Long expectedCaseHearingId = 1L;
        doReturn(expectedCaseHearingId).when(caseHearingRequestRepository).getCaseHearingId(any());
        Long caseHearingId = caseHearingRequestRepository.getCaseHearingId(any());
        assertAll(
            () -> assertThat(caseHearingId, is(expectedCaseHearingId)),
            () -> verify(caseHearingRequestRepository, times(1)).getCaseHearingId(any())
        );
    }

    @Test
    void testGetCaseHearing() {
        doReturn(TestingUtil.caseHearingRequestEntity()).when(caseHearingRequestRepository).getCaseHearing(any());
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestRepository.getCaseHearing(any());
        assertAll(
            () -> assertNotNull(caseHearingRequestEntity),
            () -> verify(caseHearingRequestRepository, times(1)).getCaseHearing(any())
        );
    }

    @Test
    void testGetHmctsServiceCodeisValid() {
        Long expectedCount = 1L;
        doReturn(1L).when(caseHearingRequestRepository).getHmctsServiceCodeCount(any());
        Long count = caseHearingRequestRepository.getHmctsServiceCodeCount(any());
        assertAll(
            () -> assertThat(count, is(expectedCount)),
            () -> verify(caseHearingRequestRepository, times(1)).getHmctsServiceCodeCount(any())
        );
    }

    @Test
    void testGetHmctsServiceCodeisInvalid() {
        Long expectedCount = 0L;
        doReturn(0L).when(caseHearingRequestRepository).getHmctsServiceCodeCount(any());
        Long count = caseHearingRequestRepository.getHmctsServiceCodeCount(any());
        assertAll(
            () -> assertThat(count, is(expectedCount)),
            () -> verify(caseHearingRequestRepository, times(1)).getHmctsServiceCodeCount(any())
        );
    }

}
