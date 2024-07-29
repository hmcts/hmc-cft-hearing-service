package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMI;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_FAILURE_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SUCCESS_STATUS;

class LinkedHearingStatusAuditServiceTest {

    @InjectMocks
    private LinkedHearingStatusAuditServiceImpl linkedHearingStatusAuditService;

    @Mock
    ObjectMapperService objectMapperService;

    @Mock
    LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linkedHearingStatusAuditService =
            new LinkedHearingStatusAuditServiceImpl(linkedHearingStatusAuditRepository, objectMapperService);
    }

    @Nested
    class LinkedHearingStatusAuditDetails {
        @Test
        void shouldSaveLinkedAuditTriageDetailsWhenFailure() {
            LinkedGroupDetails linkedGroupDetails =  TestingUtil.linkedGroupDetailsEntity();
            List<HearingEntity> hearingEntities = List.of(TestingUtil.hearingEntityWithLinkDetails());
            JsonNode errorDetails = objectMapperService.convertObjectToJsonNode("005 rejected by List Assist");
            given(linkedHearingStatusAuditRepository.save(TestingUtil.linkedHearingStatusAuditEntity())).willReturn(
                TestingUtil.linkedHearingStatusAuditEntity());
            linkedHearingStatusAuditService.saveLinkedHearingAuditTriageDetails(HMC,linkedGroupDetails,
                                                              CREATE_HEARING_REQUEST, LA_FAILURE_STATUS, HMI,
                                                                                errorDetails,hearingEntities);
            verify(linkedHearingStatusAuditRepository, times(1)).save(any());

        }

        @Test
        void shouldSaveAuditTriageDetailsWhenSuccess() {
            LinkedGroupDetails linkedGroupDetails =  TestingUtil.linkedGroupDetailsEntity();
            List<HearingEntity> hearingEntities = List.of(TestingUtil.hearingEntityWithLinkDetails());
            given(linkedHearingStatusAuditRepository.save(TestingUtil.linkedHearingStatusAuditEntity())).willReturn(
                TestingUtil.linkedHearingStatusAuditEntity());
            linkedHearingStatusAuditService.saveLinkedHearingAuditTriageDetails(HMC,linkedGroupDetails,
                                                                                CREATE_HEARING_REQUEST,
                                                                                SUCCESS_STATUS, HMI,
                                                                                null, hearingEntities);
            verify(linkedHearingStatusAuditRepository, times(1)).save(any());
        }
    }

}
