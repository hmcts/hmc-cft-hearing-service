package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingActualsService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@RestController
public class HearingActualsManagementController {

    private final HearingActualsService hearingActualsService;
    private final AccessControlService accessControlService;
    private final SecurityUtils securityUtils;

    public HearingActualsManagementController(HearingActualsService hearingActualsService,
            AccessControlService accessControlService,
            SecurityUtils securityUtils) {
        this.hearingActualsService = hearingActualsService;
        this.accessControlService = accessControlService;
        this.securityUtils = securityUtils;
    }

    @PutMapping(path = "/hearingActuals/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Hearing actuals update processed")
    @ApiResponse(responseCode = "400", description = "One or more of the following reasons:"
        + "\n1) " + ValidationError.HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND
        + "\n2) " + ValidationError.INVALID_HEARING_ID_DETAILS
        + "\n3) " + ValidationError.HEARING_ACTUALS_ID_NOT_FOUND
        + "\n4) " + ValidationError.HEARING_ACTUALS_INVALID_STATUS
        + "\n5) " + ValidationError.HEARING_ACTUALS_HEARING_DAYS_INVALID
        + "\n6) " + ValidationError.HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS)
    @ApiResponse(responseCode = "500", description = ValidationError.INTERNAL_SERVER_ERROR)

    public void updateHearingActuals(@PathVariable("id") Long hearingId,
            @RequestHeader(SERVICE_AUTHORIZATION) String clientS2SToken,
            @RequestBody @Valid HearingActual request) {
        accessControlService.verifyHearingCaseAccess(hearingId, Lists.newArrayList(HEARING_MANAGER));
        hearingActualsService.updateHearingActuals(hearingId, getServiceName(clientS2SToken), request);
    }

    private String getServiceName(String clientS2SToken) {
        return securityUtils.getServiceNameFromS2SToken(clientS2SToken);
    }
}
