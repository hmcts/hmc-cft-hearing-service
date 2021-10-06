package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import static uk.gov.hmcts.reform.hmc.constants.Constants.INVALID_REQUEST_DETAILS;

@Service
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    @Override
    public void validateHearingRequest(HearingRequest hearingRequest) {
        isValidRequestDetails(hearingRequest.getHearingDetails());
    }

    private void isValidRequestDetails(HearingDetails hearingDetails) {
        log.info("Validating HearingDetails");
        if (hearingDetails == null) {
            throw new BadRequestException(INVALID_REQUEST_DETAILS);
        }
    }
}
