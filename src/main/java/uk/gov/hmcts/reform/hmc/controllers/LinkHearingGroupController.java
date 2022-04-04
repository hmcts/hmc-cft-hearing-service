package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.LinkedHearingGroupService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
    public void validateLinkHearing(@RequestBody @Valid HearingLinkGroupRequest hearingLinkGroupRequest) {
        accessControlService.verifyCaseAccess(null);
        linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
    }

}
