package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class HearingManagementController {

    private HearingManagementService hearingManagementService;

    public HearingManagementController(HearingManagementService hearingManagementService) {
        this.hearingManagementService = hearingManagementService;
    }

    @PostMapping(path = "/hearing", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Hearing id is valid"),
        @ApiResponse(code = 404, message = "Invalid hearing id")
    })
    public void getHearing(@RequestBody String json) {

        hearingManagementService.sendResponse(json);
    }
}
