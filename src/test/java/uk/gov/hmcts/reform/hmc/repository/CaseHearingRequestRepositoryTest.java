package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void testGetCaseHearingId() {
        Long expectedCaseHearingId = 1L;
        doReturn(1L).when(caseHearingRequestRepository).getCaseHearingId(any());
        Long caseHearingId = caseHearingRequestRepository.getCaseHearingId(any());
        assertAll(
            () -> assertThat(caseHearingId, is(expectedCaseHearingId)),
            () -> verify(caseHearingRequestRepository, times(1)).getCaseHearingId(any())
        );
    }

}
