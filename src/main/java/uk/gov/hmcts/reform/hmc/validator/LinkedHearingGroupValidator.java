package uk.gov.hmcts.reform.hmc.validator;

import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

public class LinkedHearingGroupValidator extends HearingIdValidator {

    protected final LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    public LinkedHearingGroupValidator(HearingRepository hearingRepository,
                                       LinkedGroupDetailsRepository linkedGroupDetailsRepository) {
        super(hearingRepository);
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
    }

    /**
     * validate Request id.
     * @param requestId request id
     */
    protected void validateRequestId(Long requestId, String errorMessage) {
        if (requestId == null) {
            throw new BadRequestException(LINKED_GROUP_ID_EMPTY);
        } else {
            if (!linkedGroupDetailsRepository.existsById(requestId)) {
                throw new LinkedGroupNotFoundException(requestId, errorMessage);
            }
        }
    }
}
