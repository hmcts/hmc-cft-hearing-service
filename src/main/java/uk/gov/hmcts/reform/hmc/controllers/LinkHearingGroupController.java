package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.LinkedHearingGroupService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@RestController
@Validated
public class LinkHearingGroupController {

    private final LinkedHearingGroupService linkedHearingGroupService;
    private AccessControlService accessControlService;

    public LinkHearingGroupController(LinkedHearingGroupService linkedHearingGroupService,
                                      AccessControlService accessControlService) {
        this.linkedHearingGroupService = linkedHearingGroupService;
        this.accessControlService = accessControlService;
    }

    @PostMapping(path = "/linkedHearingGroup", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Success"),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.HEARINGS_IN_GROUP_SIZE
                + "\n2) " + ValidationError.HEARING_REQUEST_CANNOT_BE_LINKED
                + "\n3) " + ValidationError.HEARING_REQUEST_ALREADY_LINKED
                + "\n4) " + ValidationError.INVALID_STATE_FOR_HEARING_REQUEST
                + "\n5) " + ValidationError.HEARING_ORDER_NOT_UNIQUE)
    })
    public HearingLinkGroupResponse validateLinkHearing(@RequestBody @Valid
                                                            HearingLinkGroupRequest hearingLinkGroupRequest) {
        verifyAccess(hearingLinkGroupRequest);
        return linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
    }

    @PutMapping(path = "/linkedHearingGroup", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS
                + "\n2) " + ValidationError.HEARINGS_IN_GROUP_SIZE
                + "\n3) " + ValidationError.HEARING_REQUEST_CANNOT_BE_LINKED
                + "\n4) " + ValidationError.HEARING_REQUEST_ALREADY_LINKED
                + "\n5) " + ValidationError.INVALID_STATE_FOR_HEARING_REQUEST
                + "\n6) " + ValidationError.INVALID_STATE_FOR_LINKED_GROUP
                + "\n7) " + ValidationError.INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST)
    })
    public void updateHearing(@RequestParam("id") String requestId,
                              @RequestBody @Valid HearingLinkGroupRequest hearingLinkGroupRequest) {
        verifyAccess(hearingLinkGroupRequest);
        linkedHearingGroupService.updateLinkHearing(requestId, hearingLinkGroupRequest);
    }

    @DeleteMapping(path = "/linkedHearingGroup/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Hearing group deletion processed"),
        @ApiResponse(code = 400, message = ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS),
        @ApiResponse(code = 404, message = ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS),
        @ApiResponse(code = 500, message = ValidationError.INTERNAL_SERVER_ERROR)
    })
    public void deleteHearingGroup(@PathVariable("id") String requestId) {
        linkedHearingGroupService.deleteLinkedHearingGroup(requestId);
    }

    private void verifyAccess(HearingLinkGroupRequest request) {
        request.getHearingsInGroup().stream()
            .map(hearingGroup -> hearingGroup.getHearingId())
            .forEach(hearingId -> accessControlService.verifyAccess(
                Long.valueOf(hearingId),
                Lists.newArrayList(HEARING_MANAGER)
            ));
    }
}
