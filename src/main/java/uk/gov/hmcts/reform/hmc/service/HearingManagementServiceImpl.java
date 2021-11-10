package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;

@Service
@Component
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private HearingRepository hearingRepository;

    @Autowired
    public HearingManagementServiceImpl(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }


    @Override
    public void getHearingRequest(Long hearingId, boolean isValid) {
        if (isValid) {
            HearingEntity hearingEntity = hearingRepository.findHearing(hearingId);
            if (hearingEntity == null) {
                throw new HearingNotFoundException(hearingId);
            }
        }
    }

}
