package uk.gov.hmcts.reform.hmc.service.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;

@ExtendWith(MockitoExtension.class)
class HearingRequestVersionAuditServiceTest {

    @InjectMocks
    private HearingRequestVersionAuditService hearingRequestVersionAuditService;

    @Mock
    private HearingEntity hearingEntity;

    @Mock
    private HearingStatusAuditService hearingStatusAuditService;

    private String clientS2SToken = "client-token";

    @Test
    void shouldDoNothingWhenRequestVersionHasNotChanged() {
        when(hearingEntity.getLatestRequestVersion()).thenReturn(3);

        hearingRequestVersionAuditService.auditChangeInRequestVersion(
            hearingEntity, 3, clientS2SToken, true);

        verifyNoInteractions(hearingStatusAuditService);
    }

    @Test
    void shouldSaveCreatedDateAuditWhenExistingVersionIsZero() {
        when(hearingEntity.getLatestRequestVersion()).thenReturn(5);

        hearingRequestVersionAuditService.auditChangeInRequestVersion(
            hearingEntity, 0, clientS2SToken, true);

        ArgumentCaptor<HearingStatusAuditContext> captor =
            ArgumentCaptor.forClass(HearingStatusAuditContext.class);

        verify(hearingStatusAuditService).saveAuditTriageDetailsWithCreatedDate(captor.capture());
        verifyNoMoreInteractions(hearingStatusAuditService);

        assertAuditContext(
            captor.getValue(), false, "requestVersion set to <5>");
    }

    @Test
    void shouldSaveUpdatedDateAuditWhenExistingVersionIsGreaterThanZero() {
        when(hearingEntity.getLatestRequestVersion()).thenReturn(7);

        hearingRequestVersionAuditService.auditChangeInRequestVersion(
            hearingEntity, 2, clientS2SToken, true);

        ArgumentCaptor<HearingStatusAuditContext> captor =
            ArgumentCaptor.forClass(HearingStatusAuditContext.class);

        verify(hearingStatusAuditService).saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(captor.capture());
        verifyNoMoreInteractions(hearingStatusAuditService);

        assertAuditContext(
            captor.getValue(), true, "requestVersion updated from <2> to <7>");
    }

    private void assertAuditContext(HearingStatusAuditContext context, boolean useCurrentTimestamp,
                                    String expectedOtherInfo) {
        assertAll(
            () -> assertEquals(hearingEntity, context.getHearingEntity()),
            () -> assertEquals(REQUEST_VERSION_UPDATE, context.getHearingEvent()),
            () -> assertEquals(String.valueOf(HttpStatus.OK.value()), context.getHttpStatus()),
            () -> assertEquals(clientS2SToken, context.getSource()),
            () -> assertEquals(HMC, context.getTarget()),
            () -> assertEquals(useCurrentTimestamp, context.isUseCurrentTimestamp()),
            () -> assertEquals(
                expectedOtherInfo,
                context.getOtherInfo().get(REQUEST_VERSION_UPDATE).asText()
            )
        );
    }

}
