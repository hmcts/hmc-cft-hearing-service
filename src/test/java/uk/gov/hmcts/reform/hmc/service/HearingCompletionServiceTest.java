package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.model.HmcHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HmcHearingUpdate;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.service.common.ActualHearingAuditService;
import uk.gov.hmcts.reform.hmc.service.common.HearingRequestVersionAuditService;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class HearingCompletionServiceTest {

    private HearingCompletionService hearingCompletionService;

    @Mock
    HearingStatusAuditService hearingStatusAuditService;

    @Mock
    private MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    @Mock
    private ObjectMapperService objectMapperService;

    @Mock
    private HmiHearingResponseMapper hmiHearingResponseMapper;

    @Mock
    private HearingRequestVersionAuditService hearingRequestVersionAuditService;

    @Mock
    HearingIdValidator hearingIdValidator;

    @Mock
    private SecurityUtils securityUtils;

    @Captor
    private ArgumentCaptor<HearingStatusAuditContext> hearingStatusAuditContextCaptor;

    @Captor
    private ArgumentCaptor<HearingEntity> hearingEntityCaptor;

    @Mock
    private ActualHearingAuditService actualHearingAuditService;

    private static final String CLIENT_S2S_TOKEN = "s2s_token";
    public static final String USER_ID = "userId";
    JsonNode jsonNode = mock(JsonNode.class);

    @BeforeEach
    void setUp() {
        hearingCompletionService = new HearingCompletionService(hearingStatusAuditService,
            messageSenderToTopicConfiguration,
            objectMapperService,
            hmiHearingResponseMapper,
            securityUtils,
            hearingRequestVersionAuditService);
    }

    @ParameterizedTest(name = "[{index}] status={0}")
    @MethodSource("validFinalStatuses")
    void testCompletionHearingWithFinalStatus(HearingStatus finalStatus,
                                              HearingResultType hearingResultType) {
        final long hearingId = 2000000000L;
        final HearingEntity hearingEntity =
            setupHearingActualsStatusScenario(hearingId, finalStatus.name());
        mockHearingCompletionRequest(hearingResultType.name());
        ActualHearingEntity actualHearingEntity = new ActualHearingEntity();
        actualHearingEntity.setHearingResultType(hearingResultType);
        hearingCompletionService.completeHearing(hearingEntity, CLIENT_S2S_TOKEN,
                                                1);
        verify(hearingRequestVersionAuditService).auditChangeInRequestVersion(any(), anyInt(),any(),anyBoolean());
        verify(hearingStatusAuditService).saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(any());
        verify(messageSenderToTopicConfiguration).sendMessage(any(), any(), any(), any());
        assertAuditDetailsWithUpdatedDateOrCurrentDate();
    }

    private static Stream<Arguments> validFinalStatuses() {
        return Stream.of(
            arguments(COMPLETED.name(), HearingResultType.ADJOURNED),
            arguments(COMPLETED.name(), HearingResultType.CANCELLED),
            arguments(ADJOURNED.name(), HearingResultType.COMPLETED),
            arguments(ADJOURNED.name(), HearingResultType.CANCELLED),
            arguments(CANCELLED.name(), HearingResultType.ADJOURNED),
            arguments(CANCELLED.name(), HearingResultType.COMPLETED)
        );
    }

    private HearingEntity setupHearingActualsStatusScenario(long hearingId,
                                                            String hearingStatus) {
        UpdateHearingRequest hearingRequest = TestingUtil.updateHearingRequest();
        int versionNumber = hearingRequest.getRequestDetails().getVersionNumber();
        HearingEntity hearingEntity = generateHearingEntity(hearingId, hearingStatus, versionNumber);
        addHearingResponses(hearingEntity, 1);
        return hearingEntity;
    }

    private HearingEntity generateHearingEntity(Long hearingId, String status, Integer versionNumber) {
        HearingEntity hearingEntity = new HearingEntity();
        hearingEntity.setId(hearingId);
        hearingEntity.setStatus(status);

        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(LocalDateTime.now());
        caseHearingRequestEntity.setVersionNumber(versionNumber + 1);
        caseHearingRequestEntity.setCaseReference(CASE_REFERENCE);
        hearingEntity.setCaseHearingRequests(List.of(caseHearingRequestEntity));
        return hearingEntity;
    }

    private void addHearingResponses(HearingEntity hearingEntity,
                                     int noOfResponses) {
        List<HearingResponseEntity> responseEntities = new ArrayList<>();
        for (int i = 0; i < noOfResponses; i++) {
            HearingResponseEntity responseEntity = new HearingResponseEntity();
            responseEntity.setRequestVersion(hearingEntity.getLatestRequestVersion());
            responseEntity.setRequestTimeStamp(LocalDateTime.now());
            responseEntities.add(responseEntity);
        }
        hearingEntity.setHearingResponses(responseEntities);
    }

    private void mockHearingCompletionRequest(String finalStatus) {
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getSub()).thenReturn(USER_ID);
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        JsonNode userIdNode = mock(JsonNode.class);
        when(userIdNode.asText()).thenReturn(USER_ID);
        when(jsonNode.get("userId")).thenReturn(userIdNode);

        HmcHearingResponse hmcHearingResponse = new HmcHearingResponse();
        hmcHearingResponse.setHearingID("2000000000");
        HmcHearingUpdate hmcHearingUpdate = new HmcHearingUpdate();
        hmcHearingUpdate.setHmcStatus(finalStatus);

        hmcHearingResponse.setHearingUpdate(hmcHearingUpdate);
        when(hmiHearingResponseMapper.mapEntityToHmcModel(any(HearingResponseEntity.class),
                                                          any(HearingEntity.class))).thenReturn(hmcHearingResponse);
        when(objectMapperService.convertObjectToJsonNode(hmcHearingResponse)).thenReturn(jsonNode);
        when(objectMapperService.convertObjectToJsonNode(Map.of("userId", USER_ID))).thenReturn(jsonNode);
    }

    private void assertAuditDetailsWithUpdatedDateOrCurrentDate() {
        verify(hearingStatusAuditService)
            .saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContextCaptor.capture());
        HearingStatusAuditContext auditContext = hearingStatusAuditContextCaptor.getValue();
        assertNotNull(auditContext.getOtherInfo());
        assertEquals(USER_ID, auditContext.getOtherInfo().get("userId").asText());
    }

}
