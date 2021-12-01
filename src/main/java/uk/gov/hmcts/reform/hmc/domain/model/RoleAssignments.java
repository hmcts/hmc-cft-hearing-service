package uk.gov.hmcts.reform.hmc.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleAssignments {
    private List<RoleAssignment> roleAssignments;
}
