package uk.gov.hmcts.reform.hmc.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.PartiesNotifiedService;

import java.time.LocalDateTime;
import java.util.Arrays;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@RestController
@Validated
public class PartiesNotifiedController {

    private final PartiesNotifiedService partiesNotifiedService;
    private AccessControlService accessControlService;

    public PartiesNotifiedController(PartiesNotifiedService partiesNotifiedService,
                                     AccessControlService accessControlService) {
        this.partiesNotifiedService = partiesNotifiedService;
        this.accessControlService = accessControlService;
    }

    @PutMapping(path = "/partiesNotified/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 404,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND
                + "\n2) " + ValidationError.PARTIES_NOTIFIED_NO_SUCH_RESPONSE),
        @ApiResponse(code = 500, message = ValidationError.INTERNAL_SERVER_ERROR)
    })
    public void putPartiesNotified(@RequestBody @Valid PartiesNotified partiesNotified,
                                   @PathVariable("id") Long hearingId,
                                   @RequestParam("version") int requestVersion,
                                   @RequestParam("received")
                                   @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                                   LocalDateTime receivedDateTime) {
        accessControlService.verifyAccess(hearingId, Arrays.asList(HEARING_MANAGER));
        partiesNotifiedService.getPartiesNotified(hearingId, requestVersion, receivedDateTime, partiesNotified);
    }

    @GetMapping(path = "/partiesNotified/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Hearing id is valid"),
        @ApiResponse(code = 400,
            message = "One or more of the following reasons:"
                + "\n1) " + ValidationError.INVALID_HEARING_ID_DETAILS
                + "\n2) " + ValidationError.PARTIES_NOTIFIED_ALREADY_SET),
        @ApiResponse(code = 404, message = ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND)
    })
    public PartiesNotifiedResponses getPartiesNotified(@PathVariable("id") Long hearingId) {
        accessControlService.verifyAccess(hearingId, Arrays.asList(HEARING_MANAGER));
        return partiesNotifiedService.getPartiesNotified(hearingId);
    }
}
