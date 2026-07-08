package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingAuditRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;

@ExtendWith(MockitoExtension.class)
class ActualHearingAuditServiceImplTest {

    private ActualHearingAuditServiceImpl actualHearingAuditService;

    @Mock
    ActualHearingAuditRepository actualHearingAuditRepository;

    @Captor
    private ArgumentCaptor<ActualHearingAuditEntity> actualHearingAuditEntityArgumentCaptor;

    private static final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module(), new JavaTimeModule())
        .build();

    @BeforeEach
    void setUp() {
        actualHearingAuditService =
            new ActualHearingAuditServiceImpl(actualHearingAuditRepository, objectMapper);
    }

    @Test
    void saveActualHearingAuditDetails_passesExactEntityToRepository() {
        ActualHearingAuditEntity entity = new ActualHearingAuditEntity();
        entity.setHearingId(2000000001L);
        entity.setHearingResponseId(5L);

        actualHearingAuditService.saveActualHearingAuditDetails(entity);

        verify(actualHearingAuditRepository).save(entity);
        verify(actualHearingAuditRepository).save(actualHearingAuditEntityArgumentCaptor.capture());
        ActualHearingAuditEntity captured = actualHearingAuditEntityArgumentCaptor.getValue();
        assertThat(captured.getHearingId(), is(2000000001L));
        assertThat(captured.getHearingResponseId(), is(5L));
    }

    @Test
    void mapActualHearingAuditDetails_setsHearingIdFromHearingResponse() {
        HearingActual actual = TestingUtil.hearingActual();
        HearingResponseEntity responseEntity = getHearingResponseEntity();

        ActualHearingAuditEntity result =
            actualHearingAuditService.mapActualHearingAuditDetails(actual, responseEntity.getActualHearingEntity());

        assertThat(result.getHearingId(), is(2000000000L));
        assertThat(result.getHearingResponseId(), is(2L));
        assertNotNull(result.getActualHearingAuditRecord());
        assertNotNull(result.getAuditCreateDateTime());
        JsonNode expectedAuditRecord = objectMapper.valueToTree(actual);
        assertThat(result.getActualHearingAuditRecord(), is(expectedAuditRecord));
    }

    private HearingResponseEntity getHearingResponseEntity() {
        HearingResponseEntity responseEntity = TestingUtil.hearingResponseEntity();
        ActualHearingEntity actualHearingEntity = TestingUtil.actualHearingEntity(PartyType.IND);
        responseEntity.setActualHearingEntity(actualHearingEntity);
        actualHearingEntity.setHearingResponse(responseEntity);
        HearingEntity hearingEntity = TestingUtil.getHearingEntity(
            2000000000L, REQUEST_VERSION_UPDATE,
            "9856815055686759"
        );
        responseEntity.setHearing(hearingEntity);
        return responseEntity;
    }
}
