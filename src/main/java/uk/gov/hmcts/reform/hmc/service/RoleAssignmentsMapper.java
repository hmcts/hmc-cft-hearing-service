package uk.gov.hmcts.reform.hmc.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentRequestResponse;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleAssignmentsMapper {

    RoleAssignmentsMapper INSTANCE = Mappers.getMapper(RoleAssignmentsMapper.class);

    default RoleAssignments toRoleAssignments(RoleAssignmentRequestResponse roleAssignmentRequestResponse) {
        if (roleAssignmentRequestResponse == null) {
            return null;
        }

        var roleAssignments = map(roleAssignmentRequestResponse.getRoleAssignmentResponse());

        return roleAssignments != null ? roleAssignments : new RoleAssignments();
    }

    RoleAssignments toRoleAssignments(RoleAssignmentResponse roleAssignmentResponse);


    // additional maps which support the primary toRoleAssignments calls

    @Mapping(source = "requestedRoles", target = "roleAssignments")
    RoleAssignments map(RoleAssignmentRequestResource roleAssignmentRequest);

    List<RoleAssignment> map(List<RoleAssignmentResource> roleAssignmentResources);
}
