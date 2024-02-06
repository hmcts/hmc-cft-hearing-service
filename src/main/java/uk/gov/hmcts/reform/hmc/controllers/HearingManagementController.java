package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_DEPLOYMENT_ID;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMCTS_DEPLOYMENT_ID_MAX_SIZE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.CASE_REF_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_DEPLOYMENT_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HMCTS_DEPLOYMENT_ID_NOT_REQUIRED;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

@RestController
@Validated
public class HearingManagementController {

    private final HearingManagementService hearingManagementService;
    private final AccessControlService accessControlService;
    private final ApplicationParams applicationParams;

    public HearingManagementController(HearingManagementService hearingManagementService,
                                       AccessControlService accessControlService,
                                       ApplicationParams applicationParams) {
        this.hearingManagementService = hearingManagementService;
        this.accessControlService = accessControlService;
        this.applicationParams = applicationParams;
    }

    @GetMapping(path = "/hearing/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Hearing id is valid"),
        @ApiResponse(code = 404, message = ValidationError.HEARING_ID_NOT_FOUND),
        @ApiResponse(code = 400, message = ValidationError.INVALID_HEARING_ID_DETAILS)
    })
    public ResponseEntity<GetHearingResponse> getHearing(@PathVariable("id") Long hearingId,
                                                         @RequestParam(value = "isValid",
                                                             defaultValue = "false") boolean isValid) {
        if (!isValid) {
            // Only verify access if the user is requesting more than just confirmation of a valid hearing id
            String status = hearingManagementService.getStatus(hearingId);
            List<String> requiredRoles = Lists.newArrayList(HEARING_VIEWER);
            if (HearingStatus.LISTED.name().equals(status)) {
                requiredRoles.add(LISTED_HEARING_VIEWER);
            }

            accessControlService.verifyHearingCaseAccess(hearingId, requiredRoles);
        }
        return hearingManagementService.getHearingRequest(hearingId, isValid);
    }

    @PostMapping(path = "/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Hearing successfully created"),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.INVALID_HEARING_REQUEST_DETAILS
                + "\n2) " + ValidationError.HEARING_WINDOW_DETAILS_ARE_INVALID
                + "\n3) " + ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS
            )
    })
    public HearingResponse saveHearing(@RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false)
                                        String deploymentId,
                                       @RequestBody @Valid HearingRequest createHearingRequest) {
        verifyDeploymentIdEnabled(deploymentId);
        accessControlService.verifyCaseAccess(getCaseRef(createHearingRequest), Lists.newArrayList(HEARING_MANAGER));
        return hearingManagementService.saveHearingRequest(createHearingRequest, deploymentId);
    }

    @DeleteMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Hearing cancellation processed"),
        @ApiResponse(code = 400, message = ValidationError.INVALID_HEARING_REQUEST_DETAILS),
        @ApiResponse(code = 404, message = ValidationError.HEARING_ID_NOT_FOUND),
        @ApiResponse(code = 500, message = ValidationError.INTERNAL_SERVER_ERROR)
    })
    public HearingResponse deleteHearing(@PathVariable("id") Long hearingId,
                                         @RequestBody @Valid DeleteHearingRequest deleteRequest) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARING_MANAGER));
        return hearingManagementService.deleteHearingRequest(
            hearingId, deleteRequest);
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
        @ApiResponse(code = 200, message = "Success (with content)"),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.INVALID_HEARING_REQUEST_DETAILS
                + "\n2) " + ValidationError.CASE_REF_EMPTY
                + "\n3) " + ValidationError.CASE_REF_INVALID_LENGTH
                + "\n4) " + ValidationError.CASE_REF_INVALID
            )
    })
    public GetHearingsResponse getHearings(@PathVariable("ccdCaseRef") @Valid
                                           @NotEmpty(message = ValidationError.CASE_REF_EMPTY)
                                           @Size(min = 16, max = 16, message = ValidationError.CASE_REF_INVALID_LENGTH)
                                           @LuhnCheck(message = ValidationError.CASE_REF_INVALID,
                                               ignoreNonDigitCharacters = false)
                                               String ccdCaseRef,
                                           @RequestParam(required = false)
                                               String status) {
        return getHearingsResponse(ccdCaseRef, status);
    }

    @PutMapping(path = "/hearing/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Hearing successfully updated"),
        @ApiResponse(code = 400, message = ValidationError.INVALID_HEARING_REQUEST_DETAILS),
        @ApiResponse(code = 404, message = ValidationError.CASE_NOT_FOUND),
        @ApiResponse(code = 500, message = ValidationError.INTERNAL_SERVER_ERROR)
    })
    public HearingResponse updateHearing(@RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false)
                                                 String deploymentId,
                                         @RequestBody @Valid UpdateHearingRequest hearingRequest,
                                         @PathVariable("id") Long hearingId) {
        verifyDeploymentIdEnabled(deploymentId);
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARING_MANAGER));
        return hearingManagementService.updateHearingRequest(hearingId, hearingRequest, deploymentId);
    }

    @PostMapping(path = "/hearingActualsCompletion/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success (with no content)"),
        @ApiResponse(code = 404, message = ValidationError.HEARING_ACTUALS_ID_NOT_FOUND),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.INVALID_HEARING_REQUEST_DETAILS
                + "\n2) " + ValidationError.HEARING_ACTUALS_INVALID_STATUS
                + "\n3) " + ValidationError.HEARING_ACTUALS_UN_EXPECTED
                + "\n4) " + ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME),
        @ApiResponse(code = 500, message = ValidationError.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity hearingCompletion(@PathVariable("id") Long hearingId) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARING_MANAGER));
        return hearingManagementService.hearingCompletion(hearingId);
    }

    /**
     * get list of cases either by caseRefId OR CaseRefId/caseStatus.
     * @param ccdCaseRefs list of case Ref
     * @param status optional Status
     * @return Hearing
     */
    @Transactional
    @GetMapping(value = {"/hearings"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get hearings for list of cases")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Success (with content)"),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.INVALID_HEARING_REQUEST_DETAILS
        )
    })
    public List<GetHearingsResponse> getHearingsForListOfCases(@RequestParam List<String> ccdCaseRefs,
                                                         @RequestParam(required = false)
                                                         String status) {
        List<GetHearingsResponse> hearingsResponseList = new ArrayList<>();
        if (ccdCaseRefs.isEmpty()) {
            throw new BadRequestException(CASE_REF_EMPTY);
        }
        for (String ccdCaseRef : ccdCaseRefs) {
            GetHearingsResponse hearingsResponse = getHearingsResponse(ccdCaseRef, status);
            if (hearingsResponse.getCaseHearings().size() != 0) {
                hearingsResponseList.add(hearingsResponse);
            }
        }
        return hearingsResponseList.isEmpty() ? null : hearingsResponseList;

    }

    private GetHearingsResponse getHearingsResponse(String ccdCaseRef, String status) {
        List<String> filteredRoleAssignments =
            accessControlService.verifyCaseAccess(ccdCaseRef, Lists.newArrayList(
                HEARING_VIEWER,
                LISTED_HEARING_VIEWER));

        if (hasOnlyListedHearingViewerRoles(filteredRoleAssignments)) {
            if ((status == null || HearingStatus.LISTED.name().equals(status))) {
                status = HearingStatus.LISTED.name();
            } else {
                return hearingManagementService.getEmptyHearingsResponse(ccdCaseRef);
            }
        }

        return hearingManagementService.getHearings(ccdCaseRef, status);
    }

    private String getCaseRef(HearingRequest hearingRequest) {
        if (null == hearingRequest || null == hearingRequest.getCaseDetails()) {
            return null;
        }
        return hearingRequest.getCaseDetails().getCaseRef();
    }

    private boolean hasOnlyListedHearingViewerRoles(List<String> filteredRoleAssignments) {
        return filteredRoleAssignments.stream()
            .allMatch(roleAssignment -> roleAssignment.equals(LISTED_HEARING_VIEWER));
    }

    private void verifyDeploymentIdEnabled(String deploymentId) {
        if (applicationParams.isHmctsDeploymentIdEnabled()) {
            if (!StringUtils.isEmpty(deploymentId) && deploymentId.length() > HMCTS_DEPLOYMENT_ID_MAX_SIZE) {
                throw new BadRequestException(HMCTS_DEPLOYMENT_ID_MAX_LENGTH);
            }
        } else if (!applicationParams.isHmctsDeploymentIdEnabled() && !StringUtils.isEmpty(deploymentId)) {
            throw new BadRequestException(HMCTS_DEPLOYMENT_ID_NOT_REQUIRED);
        }
    }
}
