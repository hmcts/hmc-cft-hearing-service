package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.ManageExceptionRequest;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionResponse;

public interface ManageExceptionsService {

    ManageExceptionResponse manageExceptions(ManageExceptionRequest supportRequests, String clientS2SToken);
}
