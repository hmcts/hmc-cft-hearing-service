package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingActualsService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class HearingActualsManagementControllerTest {

    public static final long HEARING_ID = 2000000000L;

    @Mock
    private HearingActual hearingActual;

    @Mock
    private HearingActualsService hearingActualsService;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private HearingActualsManagementController controller;

    @BeforeEach
    public void setUp() {
        controller = new HearingActualsManagementController(hearingActualsService, accessControlService);
    }

    @Test
    void shouldUpdateHearingActuals() {
        controller.updateHearingActuals(HEARING_ID, hearingActual);

        verify(hearingActualsService, times(1)).updateHearingActuals(HEARING_ID, hearingActual);
    }

}
