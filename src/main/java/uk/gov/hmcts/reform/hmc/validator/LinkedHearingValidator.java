package uk.gov.hmcts.reform.hmc.validator;

import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingNotValidForUnlinkingException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

public class LinkedHearingValidator extends HearingIdValidator {

    protected final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    protected final LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    public LinkedHearingValidator(HearingRepository hearingRepository,
                                  LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                  LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        super(hearingRepository);
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingDetailsRepository = linkedHearingDetailsRepository;
    }

    /**
     * validate Request id.
     * @param requestId request id
     */
    protected final void validateRequestId(String requestId, String errorMessage) {
        if (requestId == null) {
            throw new BadRequestException(LINKED_GROUP_ID_EMPTY);
        } else {
            Long answer = linkedGroupDetailsRepository.isFoundForRequestId(requestId);
            if (null == answer || answer.intValue() == 0) {
                throw new LinkedGroupNotFoundException(requestId, errorMessage);
            }
        }
    }

    /**
     * validate linked hearings to be updated.
     * @param requestId requestId
     * @param linkHearingDetailsListPayload linkHearingDetails from payload
     */
    protected final void validateLinkedHearingsForUpdate(String requestId,
                                                         List<LinkHearingDetails> linkHearingDetailsListPayload) {
        // get existing data linkedHearingDetails
        List<LinkedHearingDetailsAudit> linkedHearingDetailsListExisting
                = linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId);

        // get obsolete linkedHearingDetails
        List<LinkedHearingDetailsAudit> linkedHearingDetailsListObsolete =
                extractObsoleteLinkedHearings(linkHearingDetailsListPayload, linkedHearingDetailsListExisting);

        // validate and get errors, if any, for obsolete linkedHearingDetails
        List<String> errors = validateObsoleteLinkedHearings(linkedHearingDetailsListObsolete);

        // if errors then throw BadRequest with error list
        if (!errors.isEmpty()) {
            throw new LinkedHearingNotValidForUnlinkingException(errors);
        }

    }

    /**
     * extract the obsolete LinkedHearingDetails.
     * @param hearingDetailsListPayload  payload LinkedHearingDetails
     * @param hearingDetailsListExisting existing in db LinkedHearingDetails
     * @return obsoleteLinkedHearingDetails the obsolete LinkedHearingDetails
     */
    protected final List<LinkedHearingDetailsAudit> extractObsoleteLinkedHearings(
            List<LinkHearingDetails> hearingDetailsListPayload,
            List<LinkedHearingDetailsAudit> hearingDetailsListExisting) {
        // build list of hearing ids
        List<String> payloadHearingIds = new ArrayList<>();
        hearingDetailsListPayload.forEach(e -> payloadHearingIds.add(e.getHearingId()));

        // deduce obsolete Linked Hearing Details
        List<LinkedHearingDetailsAudit> obsoleteLinkedHearingDetails = new ArrayList<>();
        hearingDetailsListExisting.forEach(e -> {
            if (!payloadHearingIds.contains(e.getHearing().getId().toString())) {
                obsoleteLinkedHearingDetails.add(e);
            }
        });

        return obsoleteLinkedHearingDetails;

    }

    /**
     * validate obsolete Linked Hearings.
     * @param obsoleteLinkedHearings obsolete linked hearing details
     * @return errorMessages error messages
     */
    protected final List<String> validateObsoleteLinkedHearings(
            List<LinkedHearingDetailsAudit> obsoleteLinkedHearings) {
        List<String> errorMessages = new ArrayList<>();
        obsoleteLinkedHearings.forEach(e -> {
            if (!PutHearingStatus.isValid((e.getHearing().getStatus()))) {
                errorMessages.add("008 Invalid state for unlinking hearing request <hearingId>"
                        .replace("<hearingId>", e.getHearing().getId().toString()));
            }
        });
        return errorMessages;
    }

}
