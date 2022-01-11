package uk.gov.hmcts.reform.hmc.repository;

import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;

public interface RoleAssignmentRepository {

    RoleAssignmentResponse getRoleAssignments(String userId);

}
