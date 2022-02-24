package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.model.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.ArrayList;
import java.util.List;

@Service
@Component
@Slf4j
public class PartiesNotifiedServiceImpl extends HearingIdValidator implements PartiesNotifiedService {

    private final HearingResponseRepository hearingResponseRepository;

    @Autowired
    public PartiesNotifiedServiceImpl(HearingRepository hearingRepository,
            HearingResponseRepository hearingResponseRepository) {
        super(hearingRepository);
        this.hearingResponseRepository = hearingResponseRepository;
    }

    /**
     * get parties notified.
     * @param hearingId hearing id
     * @return  list partiesNotified
     */
    @Override
    public PartiesNotifiedResponses getPartiesNotified(Long hearingId) {
        validateHearingId(hearingId);
        List<HearingResponseEntity> entities = hearingResponseRepository.getPartiesNotified(hearingId);
        if (entities.isEmpty()) {
            log.info("No partiesNotified found for hearingId {}", hearingId);
        } else {
            HearingResponseEntity entity = entities.get(0);
            log.info("hearingId {}, partiesNotified {}",  hearingId,
                    entity.getHearingResponseId());
        }
        List<PartiesNotifiedResponse> partiesNotified = new ArrayList<>();
        entities.forEach(e -> {
            PartiesNotifiedResponse response = new PartiesNotifiedResponse();
            response.setResponseVersion(e.getResponseVersion());
            response.setResponseReceivedDateTime(e.getRequestTimeStamp());
            response.setRequestVersion(e.getRequestVersion());
            response.setPartiesNotified(e.getPartiesNotifiedDateTime());
            response.setServiceData(e.getServiceData());
            partiesNotified.add(response);
        });
        PartiesNotifiedResponses responses = new PartiesNotifiedResponses();
        responses.setResponses(partiesNotified);
        responses.setHearingID(hearingId);
        return responses;
    }

}
