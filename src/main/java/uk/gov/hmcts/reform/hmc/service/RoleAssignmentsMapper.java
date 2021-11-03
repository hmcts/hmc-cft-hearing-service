package uk.gov.hmcts.reform.hmc.service;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;

@Mapper(componentModel = "spring")
public interface RoleAssignmentsMapper {

    RoleAssignments toRoleAssignments(RoleAssignmentResponse roleAssignmentResponse);
}
