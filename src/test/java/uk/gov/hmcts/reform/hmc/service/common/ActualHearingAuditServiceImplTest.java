package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingAuditRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;

class ActualHearingAuditServiceImplTest {

    @InjectMocks
    private ActualHearingAuditServiceImpl actualHearingAuditService;

    @Mock
    ActualHearingAuditRepository actualHearingAuditRepository;

    @Captor
    private ArgumentCaptor<ActualHearingAuditEntity> actualHearingAuditEntityArgumentCaptor;

    private static final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module(), new JavaTimeModule())
        .build();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        actualHearingAuditService =
            new ActualHearingAuditServiceImpl(actualHearingAuditRepository, objectMapper);
    }

    @Test
    void saveActualHearingAuditDetails() throws JsonProcessingException {
        HearingActual actual = TestingUtil.hearingActual();
        JsonNode expectedAuditRecord = objectMapper.valueToTree(actual);
        HearingResponseEntity responseEntity = TestingUtil.hearingResponseEntity();
        HearingEntity hearingEntity = TestingUtil.getHearingEntity(
            2000000000L, REQUEST_VERSION_UPDATE,
            "9856815055686759");
        responseEntity.setHearing(hearingEntity);
        actualHearingAuditService.saveActualHearingAuditDetails(actual, responseEntity);
        assertSaveActualHearingAuditDetails(expectedAuditRecord);
    }

    private void assertSaveActualHearingAuditDetails(JsonNode expectedAuditRecord) {
        ActualHearingAuditEntity savedEntity = getSavedActualHearingAuditEntity();
        assertEquals(2000000000L,savedEntity.getHearingId());
        assertEquals(2,savedEntity.getHearingResponseId());
        assertNotNull(savedEntity.getActualHearingAuditRecord());
        assertEquals(expectedAuditRecord, savedEntity.getActualHearingAuditRecord());
        assertNotNull(savedEntity.getAuditCreateDateTime());
    }

    private ActualHearingAuditEntity getSavedActualHearingAuditEntity() {
        verify(actualHearingAuditRepository).save(actualHearingAuditEntityArgumentCaptor.capture());
        ActualHearingAuditEntity savedEntity = actualHearingAuditEntityArgumentCaptor.getValue();
        return savedEntity;
    }

}
