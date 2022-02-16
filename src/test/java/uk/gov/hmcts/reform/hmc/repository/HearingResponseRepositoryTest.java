package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class HearingResponseRepositoryTest {

    protected static final Logger logger = LoggerFactory.getLogger(HearingResponseRepositoryTest.class);

    @Mock
    private HearingResponseRepository hearingResponseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHearingResponsesIsEmpty() {
        List<HearingResponseEntity> responsesDefault = new ArrayList<>();
        doReturn(responsesDefault).when(hearingResponseRepository).getHearingResponses(any());
        List<LocalDateTime> dateTimes = hearingResponseRepository.getHearingResponses(any());
        assertTrue(dateTimes.isEmpty());
    }


    @Test
    void getHearingResponsesIsNotEmpty() {
        Long hearingId = 2000000099L;
        List<LocalDateTime> partiesNotifiedDateTimes = getPartiesNotifiedDateTimes(generateResponses(hearingId));
        doReturn(partiesNotifiedDateTimes).when(hearingResponseRepository).getHearingResponses(any());
        List<LocalDateTime> dateTimes = hearingResponseRepository.getHearingResponses(any());
        assertFalse(dateTimes.isEmpty());
        assertTrue(dateTimes.size() == 2);
    }

    private List<LocalDateTime> getPartiesNotifiedDateTimes(List<HearingResponseEntity> hearingResponseEntities) {
        List<LocalDateTime> partiesNotifiedDateTimes = new ArrayList<>();
        hearingResponseEntities.stream().forEach(e -> {
            if (null != e.getPartiesNotifiedDateTime()) {
                partiesNotifiedDateTimes.add(e.getPartiesNotifiedDateTime());
                logger.info("partiesNotifiedDateTime: {}", e.getPartiesNotifiedDateTime());
            }
        });
        return partiesNotifiedDateTimes;
    }

    private List<HearingResponseEntity> generateResponses(Long hearingId) {
        List<HearingResponseEntity> entities = new ArrayList<>();

        // add partiesNotifiedDateTime
        HearingResponseEntity entity1 = generateResponse(1L, hearingId);
        entity1.setPartiesNotifiedDateTime(LocalDateTime.now().minusDays(3));
        entities.add(entity1);

        // no partiesNotifiedDateTime
        HearingResponseEntity entity2 = generateResponse(2L, hearingId);
        entities.add(entity2);

        // add partiesNotifiedDateTime
        HearingResponseEntity entity3 = generateResponse(3L, hearingId);
        entity3.setPartiesNotifiedDateTime(LocalDateTime.now().minusDays(1));
        entities.add(entity3);

        return entities;
    }

    private HearingResponseEntity generateResponse(Long hearingResponseId, Long hearingId) {
        HearingResponseEntity entity = new HearingResponseEntity();
        entity.setHearing(generateHearingEntity(hearingId));
        entity.setHearingResponseId(hearingResponseId);
        entity.setListingStatus(PutHearingStatus.HEARING_REQUESTED.name());
        entity.setListingCaseStatus(PutHearingStatus.HEARING_REQUESTED.name());
        logger.info("hearingResponseEntity: {}", entity);
        return entity;
    }

    private HearingEntity generateHearingEntity(Long hearingId) {
        HearingEntity hearing = new HearingEntity();
        hearing.setId(hearingId);
        hearing.setStatus(PutHearingStatus.HEARING_REQUESTED.name());
        logger.info("hearingEntity: {}", hearing);
        return hearing;
    }

}
