package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDateTime;
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
     * @return  list dateTimes
     */
    @Override
    public List<LocalDateTime> getPartiesNotified(Long hearingId) {
        validateHearingId(hearingId);
        List<LocalDateTime> partiesNotifiedDateTimeList = hearingResponseRepository.getHearingResponses(hearingId);
        if (partiesNotifiedDateTimeList.isEmpty()) {
            log.info("No partiesNotifiedDateTimes found for hearingId {}", hearingId);
        } else {
            log.info("hearingId {}, partiesNotifiedDateTime {}",  hearingId,
                    partiesNotifiedDateTimeList.get(0));
        }
        return partiesNotifiedDateTimeList;
    }

}
