package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PANEL_REQUIREMENTS;

@Service
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    @Override
    public void validateHearingRequest(HearingRequest hearingRequest) {
        if (hearingRequest.getHearingDetails() == null) {
            throw new BadRequestException(INVALID_HEARING_DETAILS);
        }
        if (hearingRequest.getCaseDetails() == null) {
            throw new BadRequestException(INVALID_CASE_DETAILS);
        }
        if (hearingRequest.getHearingDetails().getPanelRequirements() == null) {
            throw new BadRequestException(INVALID_PANEL_REQUIREMENTS);
        }
    }
}





