package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_WINDOW;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_RELATED_PARTY_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_DOW_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_RANGES_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_VERSION_NUMBER;

@Service
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private HearingRepository hearingRepository;

    private CaseHearingRequestRepository caseHearingRequestRepository;

    @Autowired
    public HearingManagementServiceImpl(HearingRepository hearingRepository,
                                        CaseHearingRequestRepository caseHearingRequestRepository) {
        this.hearingRepository = hearingRepository;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
    }

    @Override
    public void getHearingRequest(Long hearingId, boolean isValid) {
        if (isValid && !hearingRepository.existsById(hearingId)) {
            throw new HearingNotFoundException(hearingId);
        }
    }

    @Override
    public void validateHearingRequest(HearingRequest hearingRequest) {
        validateHearingRequestDetails(hearingRequest);
        validateHearingDetails(hearingRequest.getHearingDetails());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
        }

    }

    private void validatePartyDetails(List<PartyDetails> partyDetails) {
        for (PartyDetails partyDetail : partyDetails) {
            if ((partyDetail.getIndividualDetails() != null && partyDetail.getOrganisationDetails() != null)
                || (partyDetail.getIndividualDetails() == null && partyDetail.getOrganisationDetails() == null)) {
                throw new BadRequestException(INVALID_ORG_INDIVIDUAL_DETAILS);
            }
            if (partyDetail.getUnavailabilityDow() != null && partyDetail.getUnavailabilityDow().isEmpty()) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_DOW_DETAILS);
            }
            if (partyDetail.getUnavailabilityRanges() != null && partyDetail.getUnavailabilityRanges().isEmpty()) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_RANGES_DETAILS);
            }
            if (partyDetail.getIndividualDetails() != null
                && (partyDetail.getIndividualDetails().getRelatedParties() != null
                && partyDetail.getIndividualDetails().getRelatedParties().isEmpty())) {
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

    private void validateHearingDetails(HearingDetails hearingDetails) {
        if (hearingDetails.getHearingWindow().getHearingWindowEndDateRange() == null
            && hearingDetails.getHearingWindow().getHearingWindowStartDateRange() == null
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() == null) {
            throw new BadRequestException(INVALID_HEARING_WINDOW);
        }
    }

    @Override
    public void deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest) {
        validateHearingId(hearingId);
        validateVersionNumber(hearingId, deleteRequest.getVersionNumber());
    }

    private void validateVersionNumber(Long hearingId, Integer versionNumber) {
        Integer versionNumberFromDb = getVersionNumber(hearingId);
        if (!versionNumberFromDb.equals(versionNumber)) {
            throw new BadRequestException(INVALID_VERSION_NUMBER);
        }
    }

    private Integer getVersionNumber(Long hearingId) {
        return caseHearingRequestRepository.getVersionNumber(hearingId);
    }

    private void validateHearingId(Long hearingId) {
        if (hearingId == null) {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        } else {
            String hearingIdStr = String.valueOf(hearingId);
            isValidFormat(hearingIdStr);
            if (!hearingRepository.existsById(hearingId)) {
                throw new HearingNotFoundException(hearingId);
            }
        }

    }

    private void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
            || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }
}





