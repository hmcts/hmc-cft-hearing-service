package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;

import java.util.Map;

import static com.microsoft.applicationinsights.core.dependencies.google.common.collect.Maps.newConcurrentMap;

@Service
@Qualifier(CachedRoleAssignmentRepository.QUALIFIER)
@RequestScope
public class CachedRoleAssignmentRepository implements RoleAssignmentRepository {

    private final RoleAssignmentRepository roleAssignmentRepository;

    public static final String QUALIFIER = "cached";

    private final Map<String, RoleAssignmentResponse> roleAssignments = newConcurrentMap();

    public CachedRoleAssignmentRepository(@Qualifier(DefaultRoleAssignmentRepository.QUALIFIER)
                                              RoleAssignmentRepository roleAssignmentRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Override
    public RoleAssignmentResponse getRoleAssignments(String userId) {
        return roleAssignments.computeIfAbsent(userId, e -> roleAssignmentRepository.getRoleAssignments(userId));
    }
}
