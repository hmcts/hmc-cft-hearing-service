package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.service.LinkedHearingGroupService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class LinkedHearingGroupController {

    private LinkedHearingGroupService linkedHearingGroupService;

    public LinkedHearingGroupController(LinkedHearingGroupService linkedHearingGroupService) {
        this.linkedHearingGroupService = linkedHearingGroupService;
    }

    @GetMapping(path = "/linkedHearingGroup/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success (with content)"),
        @ApiResponse(responseCode = "400", description = ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS),
        @ApiResponse(responseCode = "404", description = ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS),
    })
    public GetLinkedHearingGroupResponse getLinkedHearingGroup(@PathVariable("id") String requestId) {
        return linkedHearingGroupService.getLinkedHearingGroupResponse(requestId);
    }
}
