package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ManageRequestStatus;
import uk.gov.hmcts.reform.hmc.exceptions.HearingValidationException;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequest;
import uk.gov.hmcts.reform.hmc.model.SupportRequestResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.ManageExceptionRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.DUPLICATE_HEARING_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LAST_GOOD_STATE;

@Service
@Slf4j
public class ManageExceptionsServiceImpl implements ManageExceptionsService {

    private final ManageExceptionRepository manageExceptionRepository;
    private final HearingStatusAuditService hearingStatusAuditService;
    private final HearingRepository hearingRepository;
    private final ObjectMapper objectMapper;

    public ManageExceptionsServiceImpl(ManageExceptionRepository manageExceptionRepository,
                                       HearingStatusAuditService hearingStatusAuditService,
                                       HearingRepository hearingRepository,
                                       ObjectMapper objectMapper) {
        this.manageExceptionRepository = manageExceptionRepository;
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.hearingRepository = hearingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ManageExceptionResponse manageExceptions(ManageExceptionRequest manageExceptionRequest,
                                                    String clientS2SToken) {
        validateHearingIdLimit(manageExceptionRequest);
        validateUniqueHearingIds(manageExceptionRequest);
        List<Long> hearingIds = extractHearingIds(manageExceptionRequest.getSupportRequest());
        List<HearingEntity> hearingEntities = getHearings(hearingIds);
        ManageExceptionResponse manageExceptionResponse = processManageExceptionDetails(
            hearingEntities,
            manageExceptionRequest,
            clientS2SToken
        );
        return manageExceptionResponse;

    }

    private ManageExceptionResponse processManageExceptionDetails(List<HearingEntity> hearingEntities,
                                                                  ManageExceptionRequest manageExceptionRequest,
                                                                  String clientS2SToken) {
        for (SupportRequest request : manageExceptionRequest.getSupportRequest()) {
            String hearingId = request.getHearingId();
            boolean isHearingPresent = hearingEntities.stream()
                .anyMatch(entity -> hearingId.equals(String.valueOf(entity.getId())));

            if (!isHearingPresent) {
                createResponse(hearingId, ManageRequestStatus.FAILURE.label, INVALID_LAST_GOOD_STATE);
            }
        }
        // Add further processing logic here if needed
        return new ManageExceptionResponse(); // Placeholder for actual response
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

    private List<Long> extractHearingIds(List<SupportRequest> supportRequest) {
        return supportRequest.stream()
            .map(request -> Long.valueOf(request.getHearingId()))
            .collect(Collectors.toList());
    }

    private List<HearingEntity> getHearings(List<Long> hearingIds) {
        return hearingRepository.getHearings(hearingIds);
    }

    private SupportRequestResponse createResponse(String hearingId, String status, String message) {
        SupportRequestResponse response = new SupportRequestResponse();
        response.setHearingId(hearingId);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }


}
