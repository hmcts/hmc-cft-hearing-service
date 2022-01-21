package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        doReturn(1).when(caseHearingRequestRepository).getVersionNumber(any());
        Integer versionNumber = caseHearingRequestRepository.getVersionNumber(any());
        assertAll(
            () -> assertThat(versionNumber, is(expectedVersionNumber)),
            () -> verify(caseHearingRequestRepository, times(1)).getVersionNumber(any())
        );
    }

    @Test
    void testGetVersionNumber_IsInvalid() {
        Integer expectedVersionNumber = 1;
        doReturn(2).when(caseHearingRequestRepository).getVersionNumber(any());
        Integer versionNumber = caseHearingRequestRepository.getVersionNumber(any());
        assertAll(
            () -> assertNotEquals(versionNumber, expectedVersionNumber),
            () -> verify(caseHearingRequestRepository, times(1)).getVersionNumber(any())
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
            () -> assertEquals("ABA1", entities.get(0).getHmctsServiceID()),
            () -> assertEquals("HEARING_REQUESTED", entities.get(0).getHearing().getStatus()),
            () -> assertEquals(2000000000L, entities.get(0).getHearing().getId()),
            () -> assertEquals(1, entities.get(0).getHearing().getHearingResponses().size()),
            () -> assertEquals("4567", entities.get(1).getCaseReference()),
            () -> assertEquals("ABA1", entities.get(1).getHmctsServiceID()),
            () -> assertEquals("HEARING_UPDATED", entities.get(1).getHearing().getStatus()),
            () -> assertEquals(2000000001L, entities.get(1).getHearing().getId()),
            () -> verify(caseHearingRequestRepository, times(1))
                .getHearingDetailsWithStatus(any(), any())
        );
    }

    @Test
    void testGetHearingDetailsWhenStatusNotPresent() {
        List<CaseHearingRequestEntity> expectedDetails = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        doReturn(expectedDetails).when(caseHearingRequestRepository).getHearingDetails(any());
        List<CaseHearingRequestEntity> entities = caseHearingRequestRepository.getHearingDetails(any());
        assertAll(
            () -> assertEquals("12345", entities.get(0).getCaseReference()),
            () -> assertEquals("ABA1", entities.get(0).getHmctsServiceID()),
            () -> assertEquals(2000000000L, entities.get(0).getHearing().getId()),
            () -> assertEquals(1, entities.get(0).getHearing().getHearingResponses().size()),
            () -> verify(caseHearingRequestRepository, times(1)).getHearingDetails(any())
        );
    }

    @Test
    void testGetCaseHearingId() {
        doReturn(TestingUtil.caseHearingRequestEntity()).when(caseHearingRequestRepository).getCaseHearing(any());
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestRepository.getCaseHearing(any());
        assertAll(
            () -> assertNotNull(caseHearingRequestEntity),
            () -> verify(caseHearingRequestRepository, times(1)).getCaseHearing(any())
        );
    }

}
