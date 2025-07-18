package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.HearingValidationException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.repository.ManageExceptionRepository;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_SERVICE_TOKEN;

@Service
@Slf4j
public class ManageExceptionsServiceImpl implements ManageExceptionsService {

    private final ManageExceptionRepository manageExceptionRepository;

    public ManageExceptionsServiceImpl(ManageExceptionRepository manageExceptionRepository) {
        this.manageExceptionRepository = manageExceptionRepository;
    }

    @Override
    public ManageExceptionResponse manageExceptions(ManageExceptionRequest manageExceptionRequest,
                                                    String clientS2SToken) {
        validateServiceToken(clientS2SToken);
        validateHearingIdLimit(manageExceptionRequest);
        validateUniqueHearingIds(manageExceptionRequest);
        getHearingDetails(manageExceptionRequest);
        return null;
    }

    private void validateServiceToken(String clientS2SToken) {
        // This method is a placeholder for actual service token validation logic.
        if (clientS2SToken == null || clientS2SToken.isEmpty()) {
            log.error(INVALID_SERVICE_TOKEN + " : {}", clientS2SToken);
            throw new InvalidRoleAssignmentException(INVALID_SERVICE_TOKEN);
        }
    }

    private void validateHearingIdLimit(ManageExceptionRequest manageExceptionRequest) {
        if (manageExceptionRequest.getSupportRequest().size() > 100) {
            log.error("No. of hearings found in the request : {}", manageExceptionRequest.getSupportRequest().size());
            throw new HearingValidationException(INVALID_HEARING_ID_LIMIT);
        }
    }

    private void validateUniqueHearingIds(ManageExceptionRequest manageExceptionRequest) {
        Set<String> uniqueHearingIds = new HashSet<>();
        for (SupportRequest request : manageExceptionRequest.getSupportRequest()) {
            String hearingId = request.getHearingId();
            if (!uniqueHearingIds.add(hearingId)) {
                log.error("Duplicate hearing ID found: {}", hearingId);
                throw new HearingValidationException(DUPLICATE_HEARING_IDS);
            }
        }
    }

    private void getHearingDetails(ManageExceptionRequest manageExceptionRequest) {
        for (SupportRequest request : manageExceptionRequest.getSupportRequest()) {
            HearingEntity hearingEntity = manageExceptionRepository
                .findByHearingId(Long.valueOf(request.getHearingId()));

        }
    }

}
