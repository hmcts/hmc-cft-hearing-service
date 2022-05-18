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
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 400, message = "001 insufficient request ids"),
        @ApiResponse(code = 400, message = "002 hearing request is linked is false"),
        @ApiResponse(code = 400, message = "003 hearing request already in a group"),
        @ApiResponse(code = 400, message = "004 invalid state of hearing for request"),
        @ApiResponse(code = 400, message = "005 Hearing Order is not unique")
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
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 400, message = "001 insufficient request ids"),
            @ApiResponse(code = 400, message = "002 hearing request is linked is false"),
            @ApiResponse(code = 400, message = "003 hearing request already in a group"),
            @ApiResponse(code = 400, message = "004 invalid state of hearing for request"),
            @ApiResponse(code = 400, message = "007 group is in a <state> state"),
            @ApiResponse(code = 400, message = "008 invalid state for unlinking hearing request <hearingid>")
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
        @ApiResponse(code = 400, message = "Invalid hearing group details found"),
        @ApiResponse(code = 404, message = "Hearing Group id not found"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
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
