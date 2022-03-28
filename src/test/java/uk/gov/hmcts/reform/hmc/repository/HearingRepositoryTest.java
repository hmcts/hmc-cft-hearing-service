package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HearingRepositoryTest {

    @Mock
    private HearingRepository hearingRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetVersionNumber() {
        String expectedStatus = "AWAITING_LISTING";
        doReturn("AWAITING_LISTING").when(hearingRepository).getStatus(any());
        String status = hearingRepository.getStatus(any());
        assertAll(
            () -> assertThat(status, is(expectedStatus)),
            () -> verify(hearingRepository, times(1)).getStatus(any())
        );
    }

    @Test
    void testSaveHearingForLinkedHearing() {
        HearingEntity entity = new HearingEntity();
        entity.setStatus("HEARING_REQUESTED");
        entity.setId(2L);
        entity.setLinkedGroupDetails(null);
        entity.setLinkedOrder(null);
        entity.setIsLinkedFlag(false);
        when(hearingRepository.save(any())).thenReturn(entity);
        HearingEntity savedEntity = hearingRepository.save(entity);
        assertEquals("HEARING_REQUESTED", savedEntity.getStatus());
        assertNull(savedEntity.getLinkedOrder());
        assertNull(savedEntity.getLinkedGroupDetails());
    }
}
