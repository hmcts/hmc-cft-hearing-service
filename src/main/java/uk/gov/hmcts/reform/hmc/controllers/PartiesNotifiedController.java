package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
public class PartiesNotifiedController {

    private final HearingManagementService hearingManagementService;

    public PartiesNotifiedController(HearingManagementService hearingManagementService) {
        this.hearingManagementService = hearingManagementService;
    }

    @GetMapping(path = "/partiesNotified/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Hearing id is valid"),
            @ApiResponse(code = 400, message = "Invalid hearing id"),
            @ApiResponse(code = 404, message = "Hearing id not found")
    })
    public List<LocalDateTime> getPartiesNotified(@PathVariable("id") Long hearingId) {
        return hearingManagementService.getPartiesNotified(hearingId);
    }

}
