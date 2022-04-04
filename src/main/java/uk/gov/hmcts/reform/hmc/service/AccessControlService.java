package uk.gov.hmcts.reform.hmc.service;

import java.util.List;

public interface AccessControlService {

    void verifyCaseAccess(String caseReference, List<String> requiredRoles);

    void verifyHearingCaseAccess(Long hearingId, List<String> requiredRoles);

}
