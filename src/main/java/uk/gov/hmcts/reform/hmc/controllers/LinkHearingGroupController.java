package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.service.LinkedHearingGroupService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
public class LinkHearingGroupController {


    private final LinkedHearingGroupService linkedHearingGroupService;

    public LinkHearingGroupController(LinkedHearingGroupService linkedHearingGroupService) {
        this.linkedHearingGroupService = linkedHearingGroupService;
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
    public void validateLinkHearing(@RequestBody @Valid HearingLinkGroupRequest hearingLinkGroupRequest)
        throws JsonProcessingException {
        try {
            linkedHearingGroupService.linkHearing(hearingLinkGroupRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

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
        linkedHearingGroupService.deleteLinkedHearingGroup(hearingGroupId);
    }

}
