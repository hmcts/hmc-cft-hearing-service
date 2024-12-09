package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_MISSING_REQUIRED;

@Service
@Slf4j
public class AccessControlServiceImpl implements AccessControlService {

    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;
    private final DataStoreRepository dataStoreRepository;
    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HearingRepository hearingRepository;
    private final ApplicationParams applicationParams;

    public static final String HEARING_MANAGER = "hearing-manager";
    public static final String HEARING_VIEWER = "hearing-viewer";
    public static final String LISTED_HEARING_VIEWER = "listed-hearing-viewer";

    private static final List<String> HMC_ROLE_NAMES = Lists.newArrayList(
        HEARING_MANAGER,
        HEARING_VIEWER,
        LISTED_HEARING_VIEWER);

    public AccessControlServiceImpl(RoleAssignmentService roleAssignmentService,
                                    SecurityUtils securityUtils,
                                    @Qualifier("defaultDataStoreRepository")
                                        DataStoreRepository dataStoreRepository,
                                    CaseHearingRequestRepository caseHearingRequestRepository,
                                    HearingRepository hearingRepository,
                                    ApplicationParams applicationParams) {
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.dataStoreRepository = dataStoreRepository;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.hearingRepository = hearingRepository;
        this.applicationParams = applicationParams;
    }

    @Override
    public void verifyAccess(Long hearingId, List<String> requiredRoles) {
        if (!applicationParams.isAccessControlEnabled()) {
            return;
        }
        List<RoleAssignment> filteredRoleAssignments = verifyRoleAccess(requiredRoles);
        verifyHearingStatus(filteredRoleAssignments, null, null, hearingId);
    }

    @Override
    public List<RoleAssignment> verifyUserRoleAccess(List<String> requiredRoles) {
        if (!applicationParams.isAccessControlEnabled()) {
            return Collections.emptyList();
        }
        return verifyRoleAccess(requiredRoles);
    }

    @Override
    public List<String> verifyCaseAccess(String caseReference, List<String> requiredRoles,
                                         DataStoreCaseDetails caseDetails) {
        if (!applicationParams.isAccessControlEnabled()) {
            return Collections.emptyList();
        }
        return verifyCaseAccess(caseReference, requiredRoles, null, caseDetails);
    }

    public List<String> verifyCaseAccess(String caseReference, List<String> requiredRoles, Long hearingId,
                                         DataStoreCaseDetails caseDetails) {
        List<RoleAssignment> filteredRoleAssignments = verifyRoleAccess(requiredRoles);

        if (caseDetails == null) {
            caseDetails = dataStoreRepository.findCaseByCaseIdUsingExternalApi(caseReference);
        }

        List<RoleAssignment> roleAssignmentsMatches = checkRoleAssignmentMatchesCaseDetails(
            caseDetails,
            filteredRoleAssignments
        );
        if (roleAssignmentsMatches.isEmpty()) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES);
        }
        verifyHearingStatus(
            filteredRoleAssignments,
            caseDetails.getCaseTypeId(),
            caseDetails.getJurisdiction(),
            hearingId
        );
        return roleAssignmentsMatches.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList());
    }

    private List<RoleAssignment> verifyRoleAccess(List<String> requiredRoles) {
        RoleAssignments roleAssignments = roleAssignmentService.getRoleAssignments(securityUtils.getUserId());
        if (roleAssignments.getRoleAssignments().isEmpty()) {
            throw new ResourceNotFoundException(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, securityUtils.getUserId()));
        }
        List<RoleAssignment> filteredRoleAssignments = filterRoleAssignments(roleAssignments);
        if (filteredRoleAssignments.isEmpty()) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ROLE);
        }

        return verifyRequiredRolesExistsAndFilter(requiredRoles, filteredRoleAssignments);
    }

    @Override
    public void verifyHearingCaseAccess(Long hearingId, List<String> requiredRoles) {
        if (!applicationParams.isAccessControlEnabled()) {
            return;
        }
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestRepository
            .getLatestCaseHearingRequest(hearingId);
        if (caseHearingRequestEntity != null) {
            verifyCaseAccess(caseHearingRequestEntity.getCaseReference(), requiredRoles, hearingId, null);
        }
    }

    private List<RoleAssignment> verifyRequiredRolesExistsAndFilter(List<String> requiredRoles,
                                                                    List<RoleAssignment> filteredRoleAssignments) {
        List<RoleAssignment> requiredRoleAssignments = Lists.newArrayList();
        if (!requiredRoles.isEmpty()) {
            requiredRoleAssignments = filteredRoleAssignments.stream()
                .filter(roleAssignment -> requiredRoles.contains(roleAssignment.getRoleName()))
                .collect(Collectors.toList());

            if (requiredRoleAssignments.isEmpty()) {
                throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_MISSING_REQUIRED);
            }
        }
        return requiredRoleAssignments;
    }

    private void verifyHearingStatus(List<RoleAssignment> filteredRoleAssignments,
                                     String caseTypeId,
                                     String jurisdictionId,
                                     Long hearingId) {
        if (hearingId != null) {
            String status = hearingRepository.getStatus(hearingId);
            if (!HearingStatus.LISTED.name().equals(status) && filteredRoleAssignments
                .stream()
                .filter(roleAssignment -> checkCaseType(roleAssignment.getAttributes(), caseTypeId)
                    && checkJurisdiction(roleAssignment.getAttributes(), jurisdictionId))
                .allMatch(roleAssignment -> LISTED_HEARING_VIEWER.equals(roleAssignment.getRoleName()))) {
                throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_MISSING_REQUIRED);
            }
        }
    }

    private List<RoleAssignment> filterRoleAssignments(RoleAssignments roleAssignments) {
        return roleAssignments.getRoleAssignments()
            .stream()
            .filter(RoleAssignment::isNotExpiredRoleAssignment)
            .filter(roleAssignment -> roleAssignment.getRoleType().equalsIgnoreCase("ORGANISATION")
                && HMC_ROLE_NAMES.contains(roleAssignment.getRoleName()))
            .collect(Collectors.toList());
    }

    private List<RoleAssignment> checkRoleAssignmentMatchesCaseDetails(DataStoreCaseDetails caseDetails,
                                                                       List<RoleAssignment> roleAssignments) {
        return roleAssignments.stream()
            .filter(roleAssignment -> checkJurisdiction(roleAssignment.getAttributes(), caseDetails.getJurisdiction())
                && checkCaseType(roleAssignment.getAttributes(), caseDetails.getCaseTypeId()))
            .collect(Collectors.toList());

    }

    @SuppressWarnings("java:S2789")
    private boolean checkJurisdiction(RoleAssignmentAttributes attributes, String jurisdiction) {
        return jurisdiction == null || attributes.getJurisdiction() == null || attributes.getJurisdiction().isEmpty()
            || attributes.getJurisdiction().orElse("").equals(jurisdiction);
    }

    @SuppressWarnings("java:S2789")
    private boolean checkCaseType(RoleAssignmentAttributes attributes, String caseType) {
        return caseType == null || attributes.getCaseType() == null || attributes.getCaseType().isEmpty()
            || attributes.getCaseType().orElse("").equals(caseType);
    }
}
