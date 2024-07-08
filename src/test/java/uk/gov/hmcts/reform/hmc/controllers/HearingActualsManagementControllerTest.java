package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingActualsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class HearingActualsManagementControllerTest {

    public static final long HEARING_ID = 2000000000L;

    private static final String CLIENT_S2S_TOKEN = "xui_webapp";

    @Mock
    private HearingActual hearingActual;

    @Mock
    private HearingActualsService hearingActualsService;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private HearingActualsManagementController controller;

    @Mock
    SecurityUtils securityUtils;

    @BeforeEach
    public void setUp() {
        controller = new HearingActualsManagementController(hearingActualsService, accessControlService, securityUtils);
        doReturn("xui_webapp").when(securityUtils)
            .getServiceNameFromS2SToken(any());
    }

    @Test
    void shouldUpdateHearingActuals() {
        controller.updateHearingActuals(HEARING_ID, CLIENT_S2S_TOKEN, hearingActual);

        verify(hearingActualsService, times(1)).updateHearingActuals(HEARING_ID,
                                                                     CLIENT_S2S_TOKEN,hearingActual);
    }

}
