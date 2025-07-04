package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.LinkedHearingGroupService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@RestController
@Validated
public class LinkHearingGroupController {

    private final LinkedHearingGroupService linkedHearingGroupService;
    private AccessControlService accessControlService;
    private final SecurityUtils securityUtils;

    public LinkHearingGroupController(LinkedHearingGroupService linkedHearingGroupService,
            AccessControlService accessControlService,
            SecurityUtils securityUtils) {
        this.linkedHearingGroupService = linkedHearingGroupService;
        this.accessControlService = accessControlService;
        this.securityUtils = securityUtils;
    }

    @PostMapping(path = "/linkedHearingGroup", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Success")
    @ApiResponse(responseCode = "400", description = "One or more of the following reasons:"
        + "\n1) " + ValidationError.HEARINGS_IN_GROUP_SIZE
        + "\n2) " + ValidationError.HEARING_REQUEST_CANNOT_BE_LINKED
        + "\n3) " + ValidationError.HEARING_REQUEST_ALREADY_LINKED
        + "\n4) " + ValidationError.INVALID_STATE_FOR_HEARING_REQUEST
        + "\n5) " + ValidationError.HEARING_ORDER_NOT_UNIQUE)

    public HearingLinkGroupResponse validateLinkHearing(
            @RequestHeader(SERVICE_AUTHORIZATION) String clientS2SToken,
            @RequestBody @Valid HearingLinkGroupRequest hearingLinkGroupRequest) {
        verifyAccess(hearingLinkGroupRequest);
        return linkedHearingGroupService.linkHearing(hearingLinkGroupRequest, getServiceName(clientS2SToken));
    }

    @PutMapping(path = "/linkedHearingGroup", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "One or more of the following reasons:"
        + "\n1) " + ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS
        + "\n2) " + ValidationError.HEARINGS_IN_GROUP_SIZE
        + "\n3) " + ValidationError.HEARING_REQUEST_CANNOT_BE_LINKED
        + "\n4) " + ValidationError.HEARING_REQUEST_ALREADY_LINKED
        + "\n5) " + ValidationError.INVALID_STATE_FOR_HEARING_REQUEST
        + "\n6) " + ValidationError.INVALID_STATE_FOR_LINKED_GROUP
        + "\n7) " + ValidationError.INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST)

    public void updateLinkedHearingGroup(@RequestHeader(SERVICE_AUTHORIZATION) String clientS2SToken,
                                         @RequestParam("id") String requestId,
                                         @RequestBody @Valid HearingLinkGroupRequest hearingLinkGroupRequest) {
        verifyAccess(hearingLinkGroupRequest);
        linkedHearingGroupService.updateLinkHearing(requestId, hearingLinkGroupRequest, getServiceName(clientS2SToken));
    }

    @DeleteMapping(path = "/linkedHearingGroup/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Hearing group deletion processed")
    @ApiResponse(responseCode = "400", description = ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS)
    @ApiResponse(responseCode = "404", description = ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS)
    @ApiResponse(responseCode = "500", description = ValidationError.INTERNAL_SERVER_ERROR)

    public void deleteHearingGroup(@RequestHeader(SERVICE_AUTHORIZATION) String clientS2SToken,
            @PathVariable("id") String requestId) {
        linkedHearingGroupService.deleteLinkedHearingGroup(requestId, getServiceName(clientS2SToken));
    }

    private void verifyAccess(HearingLinkGroupRequest request) {
        request.getHearingsInGroup().stream()
                .map(hearingGroup -> hearingGroup.getHearingId())
                .forEach(hearingId -> accessControlService.verifyAccess(
                        Long.valueOf(hearingId),
                        Lists.newArrayList(HEARING_MANAGER)));
    }

    private String getServiceName(String clientS2SToken) {
        return securityUtils.getServiceNameFromS2SToken(clientS2SToken);
    }
}
