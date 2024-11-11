package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.PartiesNotifiedService;

import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@RestController
@Validated
public class PartiesNotifiedController {

    private final PartiesNotifiedService partiesNotifiedService;
    private AccessControlService accessControlService;
    private final SecurityUtils securityUtils;

    public PartiesNotifiedController(PartiesNotifiedService partiesNotifiedService,
            AccessControlService accessControlService,
            SecurityUtils securityUtils) {
        this.partiesNotifiedService = partiesNotifiedService;
        this.accessControlService = accessControlService;
        this.securityUtils = securityUtils;
    }

    @PutMapping(path = "/partiesNotified/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "404", description = "One or more of the following reasons:"
        + "\n1) " + ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND
        + "\n2) " + ValidationError.PARTIES_NOTIFIED_NO_SUCH_RESPONSE)
    @ApiResponse(responseCode = "500", description = ValidationError.INTERNAL_SERVER_ERROR)

    public void putPartiesNotified(@RequestBody @Valid PartiesNotified partiesNotified,
        @RequestHeader(SERVICE_AUTHORIZATION) String clientS2SToken,
        @PathVariable("id") Long hearingId,
        @RequestParam("version") int requestVersion,
        @RequestParam("received") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime receivedDateTime) {
        accessControlService.verifyAccess(hearingId, Lists.newArrayList(HEARING_MANAGER));
        partiesNotifiedService.getPartiesNotified(hearingId, requestVersion, receivedDateTime, partiesNotified,
                getServiceName(clientS2SToken));
    }

    @GetMapping(path = "/partiesNotified/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Hearing id is valid")
    @ApiResponse(responseCode = "400", description = "One or more of the following reasons:"
        + "\n1) " + ValidationError.INVALID_HEARING_ID_DETAILS
        + "\n2) " + ValidationError.PARTIES_NOTIFIED_ALREADY_SET)
    @ApiResponse(responseCode = "404", description = ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND)

    public PartiesNotifiedResponses getPartiesNotified(@PathVariable("id") Long hearingId) {
        accessControlService.verifyAccess(hearingId, Lists.newArrayList(HEARING_MANAGER));
        return partiesNotifiedService.getPartiesNotified(hearingId);
    }

    private String getServiceName(String clientS2SToken) {
        return securityUtils.getServiceNameFromS2SToken(clientS2SToken);
    }

}
