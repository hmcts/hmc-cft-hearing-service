package uk.gov.hmcts.reform.hmc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

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
        if (isValid && !hearingRepository.existsById(hearingId)) {
            throw new HearingNotFoundException(hearingId);
        }
    }

    @Override
    public void deleteHearingRequest(String hearingId, DeleteHearingRequest deleteRequest) {
        validateHearingId(hearingId);
        validateVersionNumber(deleteRequest.getVersionNumber());
    }

    private void validateVersionNumber(Integer versionNumber) {
        // db call
    }

    private void validateHearingId(String hearingId) {
        if (hearingId == null || hearingId.length() > HEARING_ID_MAX_LENGTH) {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        } else if(!hearingRepository.existsById(Long.valueOf(hearingId))) {
            throw new HearingNotFoundException(Long.valueOf(hearingId));
        }
        // compare if it starts with 2

    }
}
