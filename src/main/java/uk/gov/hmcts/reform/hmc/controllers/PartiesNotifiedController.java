package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.service.PartiesNotifiedService;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
public class PartiesNotifiedController {

    private final PartiesNotifiedService partiesNotifiedService;

    public PartiesNotifiedController(PartiesNotifiedService partiesNotifiedService) {
        this.partiesNotifiedService = partiesNotifiedService;
    }

    @PutMapping(path = "/partiesNotified/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 404, message = "001 no such id"),
        @ApiResponse(code = 404, message = "002 no such response version"),
        @ApiResponse(code = 500, message = "Error occurred on the server")
    })
    public void putPartiesNotified(@RequestBody @Valid PartiesNotified partiesNotified,
                                   @PathVariable("id") Long hearingId,
                                   @RequestParam("version") int responseVersion) {
        partiesNotifiedService.getPartiesNotified(hearingId, responseVersion, partiesNotified);
    }
}
