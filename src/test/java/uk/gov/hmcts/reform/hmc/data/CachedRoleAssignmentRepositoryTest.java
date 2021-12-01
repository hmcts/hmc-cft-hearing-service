package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.repository.CachedRoleAssignmentRepository;
import uk.gov.hmcts.reform.hmc.repository.RoleAssignmentRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CachedRoleAssignmentRepositoryTest {

    private static final String USER_ID = "user1";

    @Mock
    private RoleAssignmentRepository roleAssignmentRepositoryMock;

    @Mock
    private RoleAssignmentResponse roleAssignmentResponse;

    @InjectMocks
    private CachedRoleAssignmentRepository classUnderTest;

    @Test
    @DisplayName("should initially retrieve role assignments from decorated repository")
    void shouldGetRoleAssignmentsFromDefaultRepository() {

        // GIVEN
        doReturn(roleAssignmentResponse).when(roleAssignmentRepositoryMock).getRoleAssignments(USER_ID);

        // WHEN 1
        RoleAssignmentResponse roleAssignments = classUnderTest.getRoleAssignments(USER_ID);

        // THEN 1
        assertAll(
            () -> assertThat(roleAssignments, is(roleAssignmentResponse)),
            () -> verify(roleAssignmentRepositoryMock).getRoleAssignments(USER_ID)
        );

        // WHEN 2
        RoleAssignmentResponse roleAssignments2 = classUnderTest.getRoleAssignments(USER_ID);

        // THEN 2
        assertAll(
            () -> assertThat(roleAssignments2, is(roleAssignmentResponse)),
            () -> verifyNoMoreInteractions(roleAssignmentRepositoryMock)
        );
    }

}

