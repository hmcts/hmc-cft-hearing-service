package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.reform.hmc.PartiesNotifiedCommonGeneration;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FIRST_PAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;

class HearingResponseRepositoryTest extends PartiesNotifiedCommonGeneration {

    @Mock
    private HearingResponseRepository hearingResponseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHearingResponsesIsEmpty() {
        List<HearingResponseEntity> responsesDefault = new ArrayList<>();
        doReturn(responsesDefault).when(hearingResponseRepository).getPartiesNotified(any());
        List<HearingResponseEntity> responseEntities = hearingResponseRepository.getPartiesNotified(any());
        assertTrue(responseEntities.isEmpty());
    }

    @Test
    void getHearingResponsesIsNotEmpty() {
        Long hearingId = 2000000099L;
        List<HearingResponseEntity> partiesNotifiedDefault = generateEntities(hearingId);
        doReturn(partiesNotifiedDefault).when(hearingResponseRepository).getPartiesNotified(any());
        List<HearingResponseEntity> partiesNotified = hearingResponseRepository.getPartiesNotified(any());
        assertFalse(partiesNotified.isEmpty());
        assertEquals(3, partiesNotified.size());
    }

    @Test
    void testGetUnNotifiedHearingsWithOutStartDateTo() {
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        List<Long> expectedHearingIds = Arrays.asList(2000000207L, 2000000206L, 2000000205L);
        Page<Long> expectedUnNotifiedHearings = new PageImpl<>(expectedHearingIds, limit, expectedHearingIds.size());
        doReturn(expectedUnNotifiedHearings).when(hearingResponseRepository).getUnNotifiedHearingsWithOutStartDateTo(
            anyString(), any(), any());
        Page<Long> hearingIds = hearingResponseRepository.getUnNotifiedHearingsWithOutStartDateTo(
            anyString(),
            any(),
            any()
        );
        assertAll(
            () -> assertThat(hearingIds.getContent(), is(expectedUnNotifiedHearings.getContent())),
            () -> verify(hearingResponseRepository, times(1)).getUnNotifiedHearingsWithOutStartDateTo(
                anyString(),
                any(),
                any()
            )
        );
    }

    @Test
    void testGetUnNotifiedHearingsWithStartDateTo() {
        Pageable limit = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        List<Long> expectedHearingIds = Arrays.asList(2000000207L, 2000000206L, 2000000205L);
        Page<Long> expectedUnNotifiedHearings = new PageImpl<>(expectedHearingIds, limit, expectedHearingIds.size());
        doReturn(expectedUnNotifiedHearings).when(hearingResponseRepository).getUnNotifiedHearingsWithStartDateTo(
            anyString(), any(), any(),any());
        Page<Long> hearingIds = hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo(
            anyString(),
            any(),
            any(),
            any()
        );
        assertAll(
            () -> assertThat(hearingIds.getContent(), is(expectedUnNotifiedHearings.getContent())),
            () -> verify(hearingResponseRepository, times(1)).getUnNotifiedHearingsWithStartDateTo(
                anyString(),
                any(),
                any(),
                any()
            )
        );
    }


}
