package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;

@Service
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private HearingRepository hearingRepository;

    @Autowired
    public HearingManagementServiceImpl() {
        //Do nothing
    }

    @Override
    public void getHearingRequest(String hearingId) {
        HearingEntity hearingEntity = hearingRepository.getHearingByHearingId(hearingId);
        if (hearingEntity == null) {
          throw new HearingNotFoundException(hearingId);
        }
    }
}





