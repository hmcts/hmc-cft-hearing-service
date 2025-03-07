package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_MISSING_REQUIRED;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessControlServiceTest {

    public static final String USER_ID = "UserId";
    public static final String ROLE_TYPE = "ORGANISATION";
    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";
    public static final String ROLE_NAME = "hearing-manager";

    private AccessControlServiceImpl accessControlService;

    @Mock
    RoleAssignmentService roleAssignmentService;

    @Mock
    SecurityUtils securityUtils;

    @Mock
    DataStoreRepository dataStoreRepository;

    @Mock
    CaseHearingRequestRepository caseHearingRequestRepository;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    ApplicationParams applicationParams;

    @BeforeEach
    void setUp() {
        doReturn(USER_ID).when(securityUtils).getUserId();
        accessControlService = new AccessControlServiceImpl(
            roleAssignmentService,
            securityUtils,
            dataStoreRepository,
            caseHearingRequestRepository,
            hearingRepository,
            applicationParams
        );
        when(applicationParams.isAccessControlEnabled()).thenReturn(true);
    }

    private void stubRoleAssignments(RoleAssignmentAttributes.RoleAssignmentAttributesBuilder builder,
                                     String roleName) {
        RoleAssignmentAttributes roleAssignmentAttributes = builder
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(roleName)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
    }

    @Test
    void shouldThrowExceptionWhenStatusIsNotListed() {
        stubRoleAssignments(RoleAssignmentAttributes.builder()
                                .jurisdiction(Optional.of(JURISDICTION))
                                .caseType(Optional.of(CASE_TYPE)), LISTED_HEARING_VIEWER);
        when(hearingRepository.getStatus(anyLong())).thenReturn(HearingStatus.UPDATE_REQUESTED.name());
        List<String> requiredRoles = Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER);
        Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> accessControlService
            .verifyAccess(1234L, requiredRoles));
        assertEquals(ROLE_ASSIGNMENT_MISSING_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionWhenStatusIsListed() {
        stubRoleAssignments(RoleAssignmentAttributes.builder()
                                .jurisdiction(Optional.of(JURISDICTION))
                                .caseType(Optional.of(CASE_TYPE)), LISTED_HEARING_VIEWER);
        when(hearingRepository.getStatus(anyLong())).thenReturn(HearingStatus.LISTED.name());
        accessControlService.verifyAccess(1234L, Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER));
    }

    @Test
    void shouldNotThrowExceptionForVerifyAccessIsAccessControlEnabledIsFalse() {
        when(applicationParams.isAccessControlEnabled()).thenReturn(false);
        accessControlService.verifyAccess(1234L, Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER));
    }

    @Test
    void shouldThrowExceptionWhenStatusNotListedForACaseType() {
        stubRoleAssignments(RoleAssignmentAttributes.builder()
                                .jurisdiction(Optional.of(JURISDICTION))
                                .caseType(Optional.of(CASE_TYPE)), LISTED_HEARING_VIEWER);
        when(hearingRepository.getStatus(anyLong())).thenReturn(HearingStatus.HEARING_REQUESTED.name());
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        when(dataStoreRepository.findCaseByCaseIdUsingExternalApi(anyString())).thenReturn(caseDetails);
        List<String> requiredRoles = Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER);
        Exception exception = assertThrows(
            InvalidRoleAssignmentException.class,
            () -> accessControlService.verifyCaseAccess(
                "1234", requiredRoles, 12345L, null
            )
        );
        assertEquals(ROLE_ASSIGNMENT_MISSING_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionWhenStatusListedForACaseType() {
        stubRoleAssignments(RoleAssignmentAttributes.builder()
                                .jurisdiction(Optional.of(JURISDICTION))
                                .caseType(Optional.of(CASE_TYPE)), LISTED_HEARING_VIEWER);
        when(hearingRepository.getStatus(anyLong())).thenReturn(HearingStatus.LISTED.name());
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        when(dataStoreRepository.findCaseByCaseIdUsingExternalApi(anyString())).thenReturn(caseDetails);
        List<String> requiredRoles = Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER);
        accessControlService.verifyCaseAccess(
            "1234", requiredRoles,
            12345L, null
        );
    }

    @Test
    void shouldNotThrowExceptionForVerifyCaseAccessIsAccessControlEnabledIsFalse() {
        when(applicationParams.isAccessControlEnabled()).thenReturn(false);
        List<String> requiredRoles = Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER);
        accessControlService.verifyCaseAccess(
            "1234",
            requiredRoles, null
        );
    }

    @Test
    void shouldNotThrowExceptionForVerifyHearingCaseAccessIsAccessControlEnabledIsFalse() {
        when(applicationParams.isAccessControlEnabled()).thenReturn(false);
        List<String> requiredRoles = Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER);
        accessControlService.verifyHearingCaseAccess(
            12345L,
            requiredRoles
        );
    }

    @Test
    void shouldNotThrowExceptionForVerifyCaseAccessRequiredRolesIsEmpty() {
        stubRoleAssignments(RoleAssignmentAttributes.builder()
                                .jurisdiction(Optional.of(JURISDICTION))
                                .caseType(Optional.of(CASE_TYPE)), LISTED_HEARING_VIEWER);
        when(hearingRepository.getStatus(anyLong())).thenReturn(HearingStatus.LISTED.name());
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .jurisdiction(JURISDICTION)
            .build();
        when(dataStoreRepository.findCaseByCaseIdUsingExternalApi(anyString())).thenReturn(caseDetails);
        List<String> requiredRoles = Lists.newArrayList();
        Exception exception = assertThrows(
            InvalidRoleAssignmentException.class,
            () -> accessControlService.verifyCaseAccess(
                "1234", requiredRoles, null)
        );
        assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRoleAssignmentsIsEmpty() {
        stubRoleAssignments(RoleAssignmentAttributes.builder()
                                .jurisdiction(Optional.of(JURISDICTION))
                                .caseType(Optional.of(CASE_TYPE)), HEARING_MANAGER);
        when(hearingRepository.getStatus(anyLong())).thenReturn(HearingStatus.LISTED.name());
        List<String> requiredRoles =  Lists.newArrayList(LISTED_HEARING_VIEWER);
        Exception exception = assertThrows(
            InvalidRoleAssignmentException.class,
            () -> accessControlService.verifyAccess(
                1234L, requiredRoles
            )
        );
        assertEquals(ROLE_ASSIGNMENT_MISSING_REQUIRED, exception.getMessage());
    }

    @Test
    void shouldReturnEmptyListForVerifyUserRoleAccessIsAccessControlEnabledIsFalse() {
        when(applicationParams.isAccessControlEnabled()).thenReturn(false);
        List<RoleAssignment> roles = accessControlService.verifyUserRoleAccess(
            Lists.newArrayList(HEARING_MANAGER, LISTED_HEARING_VIEWER));
        assertTrue(roles.isEmpty());
    }

}
