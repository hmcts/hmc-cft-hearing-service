package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;

@Service
@Component
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService, Runnable {

    private HearingRepository hearingRepository;

    @Autowired
    public HearingManagementServiceImpl(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }


    @Override
    public void getHearingRequest(String hearingId) {
       HearingEntity hearingEntity =  hearingRepository.findHearing(hearingId);
    }

    @Override
    public void run() {
        getHearingRequest("1234");
    }
}
