package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HearingManagementServiceTest {

    private HearingManagementServiceImpl hearingManagementService;

    @MockBean
    HearingRepository hearingRepository;

    @BeforeEach
    public void setUp() {
        hearingManagementService = new HearingManagementServiceImpl(hearingRepository);
    }

    @Test
    void shouldFailWithInvalidHearingId() {
        HearingEntity hearing = new HearingEntity();
        hearing.setStatus("RESPONDED");
        hearing.setId(1L);
        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingManagementService.getHearingRequest("1L");
        });
        assertEquals("No hearing found for reference: 1L", exception.getMessage());
    }


}
