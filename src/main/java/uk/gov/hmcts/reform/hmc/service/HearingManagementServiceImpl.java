package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_CATEGORIES;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_CASE_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_LOCATION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_WINDOW;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PANEL_REQUIREMENTS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_RELATED_PARTY_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_DOW_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_RANGES_DETAILS;

@Service
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    @Autowired
    public HearingManagementServiceImpl() {
        //Do nothing
    }

    @Override
    public void validateHearingRequest(HearingRequest hearingRequest) {
        validateHearingRequestDetails(hearingRequest);
        validateRequestDetails(hearingRequest.getRequestDetails());
        validateHearingDetails(hearingRequest.getHearingDetails());
        validateCaseDetails(hearingRequest.getCaseDetails());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
        }

    }

    private void validatePartyDetails(PartyDetails[] partyDetails) {
        for (PartyDetails partyDetail : partyDetails) {
            if (partyDetail.getIndividualDetails() != null && partyDetail.getOrganisationDetails() != null) {
                throw new BadRequestException(INVALID_ORG_INDIVIDUAL_DETAILS);
            }
            if (partyDetail.getIndividualDetails() == null && partyDetail.getOrganisationDetails() == null) {
                throw new BadRequestException(INVALID_ORG_INDIVIDUAL_DETAILS);
            }
            if (partyDetail.getUnavailabilityDow() != null && partyDetail.getUnavailabilityDow().length == 0) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_DOW_DETAILS);
            }
            if (partyDetail.getUnavailabilityRanges() != null && partyDetail.getUnavailabilityRanges().length == 0) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_RANGES_DETAILS);
            }
            if (partyDetail.getIndividualDetails().getRelatedParties() != null
                && partyDetail.getIndividualDetails().getRelatedParties().length == 0) {
                throw new BadRequestException(INVALID_RELATED_PARTY_DETAILS);
            }
        }
    }

    private void validateHearingRequestDetails(HearingRequest hearingRequest) {
        if (hearingRequest.getRequestDetails() == null
            && hearingRequest.getHearingDetails() == null
            && hearingRequest.getCaseDetails() == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
    }

    private void validateRequestDetails(RequestDetails requestDetails) {
        if (requestDetails == null) {
            throw new BadRequestException(INVALID_REQUEST_DETAILS);
        }
    }

    private void validateCaseDetails(CaseDetails caseDetails) {
        if (caseDetails == null) {
            throw new BadRequestException(INVALID_CASE_DETAILS);
        }

        if (caseDetails.getCaseCategories().length == 0) {
            throw new BadRequestException(INVALID_CASE_CATEGORIES);
        }
    }

    private void validateHearingDetails(HearingDetails hearingDetails) {
        if (hearingDetails == null) {
            throw new BadRequestException(INVALID_HEARING_DETAILS);
        }
        if (hearingDetails.getPanelRequirements() == null) {
            throw new BadRequestException(INVALID_PANEL_REQUIREMENTS);
        }
        if (hearingDetails.getHearingWindow().getHearingWindowEndDateRange() == null
            && hearingDetails.getHearingWindow().getHearingWindowStartDateRange() == null
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() == null) {
            throw new BadRequestException(INVALID_HEARING_WINDOW);
        }
        if (hearingDetails.getHearingLocations().length == 0) {
            throw new BadRequestException(INVALID_HEARING_LOCATION);
        }
    }
}





