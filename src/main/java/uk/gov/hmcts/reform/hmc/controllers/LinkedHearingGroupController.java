package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.LinkedHearingGroupService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class LinkedHearingGroupController {

    private LinkedHearingGroupService linkedHearingGroupService;
    private AccessControlService accessControlService;

    public LinkedHearingGroupController(LinkedHearingGroupService linkedHearingGroupService,
                                        AccessControlService accessControlService) {
        this.linkedHearingGroupService = linkedHearingGroupService;
        this.accessControlService = accessControlService;
    }

    @Transactional
    @DeleteMapping(path = "/linkedHearingGroup/{id}", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Hearing group deletion processed"),
        @ApiResponse(code = 400, message = "Invalid hearing group details found"),
        @ApiResponse(code = 404, message = "Hearing Group id not found"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public void deleteHearingGroup(@PathVariable("id") Long hearingGroupId) {
        accessControlService.verifyCaseAccess(null);
        linkedHearingGroupService.deleteLinkedHearingGroup(hearingGroupId);
    }
}
