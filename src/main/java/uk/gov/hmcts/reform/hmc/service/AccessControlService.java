package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;

import java.util.List;

public interface AccessControlService {

    void verifyAccess(Long hearingId, List<String> requiredRoles);

    List<String> verifyCaseAccess(String caseReference, List<String> requiredRoles);

    void verifyHearingCaseAccess(Long hearingId, List<String> requiredRoles);

}
