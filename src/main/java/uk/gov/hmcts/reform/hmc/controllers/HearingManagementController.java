package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_HEARING;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_INVALID_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_DAY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_UN_EXPRECTED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_CANCELLATION_PROCESSED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_IS_VALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_IS_UPDATED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_SUCCESSFULLY_CREATED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_WITH_CASE_REFERENCE_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;

@RestController
@Validated
public class HearingManagementController {

    private final HearingManagementService hearingManagementService;

    public HearingManagementController(HearingManagementService hearingManagementService) {
        this.hearingManagementService = hearingManagementService;
    }

    @GetMapping(path = "/hearing/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = HEARING_ID_IS_VALID),
        @ApiResponse(code = 404, message = HEARING_ID_NOT_FOUND),
        @ApiResponse(code = 400, message = INVALID_HEARING_ID_DETAILS)
    })
    public ResponseEntity<GetHearingResponse> getHearing(@PathVariable("id") Long hearingId,
                                                         @RequestParam(value = "isValid",
                                                             defaultValue = "false") boolean isValid) {
        return hearingManagementService.getHearingRequest(hearingId, isValid);
    }

    @PostMapping(path = "/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HEARING_SUCCESSFULLY_CREATED),
        @ApiResponse(code = 400, message = INVALID_HEARING_REQUEST_DETAILS),
        @ApiResponse(code = 403, message = CASE_NOT_FOUND)
    })
    public HearingResponse saveHearing(@RequestBody @Valid HearingRequest createHearingRequest) {
        hearingManagementService.verifyAccess(getCaseRef(createHearingRequest));
        HearingResponse hearingResponse = hearingManagementService.saveHearingRequest(createHearingRequest);
        hearingManagementService.sendRequestToHmiAndQueue(hearingResponse.getHearingRequestId(), createHearingRequest,
                REQUEST_HEARING
        );
        return hearingResponse;
    }

    @DeleteMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HEARING_CANCELLATION_PROCESSED),
        @ApiResponse(code = 400, message = INVALID_HEARING_REQUEST_DETAILS),
        @ApiResponse(code = 404, message = HEARING_ID_NOT_FOUND)
    })
    public HearingResponse deleteHearing(@PathVariable("id") Long hearingId,
                                         @RequestBody @Valid DeleteHearingRequest deleteRequest) {
        HearingResponse hearingResponse = hearingManagementService.deleteHearingRequest(
            hearingId, deleteRequest);
        hearingManagementService.sendRequestToHmiAndQueue(deleteRequest, hearingId, DELETE_HEARING);

        return hearingResponse;
    }

    /**
     * get Case either by caseRefId OR CaseRefId/caseStatus.
     * @param ccdCaseRef case Ref
     * @param status optional Status
     * @return Hearing
     */
    @Transactional
    @GetMapping(value = {"/hearings/{ccdCaseRef}"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get hearings")
    @ApiResponses({
        @ApiResponse(code = 200, message = HEARING_WITH_CASE_REFERENCE_FOUND),
        @ApiResponse(code = 400, message = INVALID_HEARING_REQUEST_DETAILS)
    })
    public GetHearingsResponse getHearings(@PathVariable("ccdCaseRef")
                                           @Valid
                                           @NotEmpty(message = CASE_REF_EMPTY)
                                           @Size(min = 16, max = 16, message = CASE_REF_INVALID_LENGTH)
                                           @LuhnCheck(message = CASE_REF_INVALID, ignoreNonDigitCharacters = false)
                                               String ccdCaseRef,
                                           @RequestParam(required = false)
                                               String status) {
        return hearingManagementService.getHearings(ccdCaseRef, status);
    }

    @PutMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HEARING_IS_UPDATED),
        @ApiResponse(code = 400, message = INVALID_HEARING_REQUEST_DETAILS),
        @ApiResponse(code = 404, message = CASE_NOT_FOUND)
    })
    public HearingResponse updateHearing(@RequestBody @Valid UpdateHearingRequest hearingRequest,
                                         @PathVariable("id") Long hearingId) {
        HearingResponse hearingResponse = hearingManagementService.updateHearingRequest(hearingId, hearingRequest);
        hearingManagementService.sendRequestToHmiAndQueue(hearingId, hearingRequest, AMEND_HEARING);
        return hearingResponse;
    }

    @PostMapping(path = "/hearingActualsCompletion/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HEARING_SUCCESSFULLY_CREATED),
        @ApiResponse(code = 404, message = HEARING_ACTUALS_ID_NOT_FOUND),
        @ApiResponse(code = 400,
            message = INVALID_HEARING_REQUEST_DETAILS
                + " | " + HEARING_ACTUALS_INVALID_STATUS
                + " | " + HEARING_ACTUALS_MISSING_HEARING_DAY
                + " | " + HEARING_ACTUALS_UN_EXPRECTED
                + " | " + HEARING_ACTUALS_MISSING_HEARING_OUTCOME)
    })
    public ResponseEntity hearingCompletion(@PathVariable("id") Long hearingId) {
        return hearingManagementService.hearingCompletion(hearingId);
    }

    private String getCaseRef(HearingRequest hearingRequest) {
        if (null == hearingRequest || null == hearingRequest.getCaseDetails()) {
            return null;
        }
        return hearingRequest.getCaseDetails().getCaseRef();
    }
}
