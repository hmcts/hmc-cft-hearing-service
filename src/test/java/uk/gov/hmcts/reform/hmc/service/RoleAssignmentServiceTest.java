package uk.gov.hmcts.reform.hmc.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.repository.RoleAssignmentRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@DisplayName("RoleAssignmentService")
@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceTest {

    private static final String USER_ID = "user1";

    @Mock
    private RoleAssignmentRepository roleAssignmentRepository;

    @Mock
    private RoleAssignmentsMapper roleAssignmentsMapper;

    @Mock
    private RoleAssignments mockedRoleAssignments;

    @Mock
    private RoleAssignmentResponse mockedRoleAssignmentResponse;

    @InjectMocks
    private RoleAssignmentService roleAssignmentService;

    @Test
    @DisplayName("getRoleAssignments()")
    void shouldGetRoleAssignments() {

        // GIVEN
        given(roleAssignmentRepository.getRoleAssignments(USER_ID))
            .willReturn(mockedRoleAssignmentResponse);
        given(roleAssignmentsMapper.toRoleAssignments(mockedRoleAssignmentResponse))
            .willReturn(mockedRoleAssignments);

        // WHEN
        RoleAssignments roleAssignments = roleAssignmentService.getRoleAssignments(USER_ID);

        // THEN
        assertThat(roleAssignments, is(mockedRoleAssignments));
    }

}
