package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
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

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_MISSING_REQUIRED;

@Service
@Slf4j
public class AccessControlServiceImpl implements AccessControlService {

    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;
    private final DataStoreRepository dataStoreRepository;
    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HearingRepository hearingRepository;

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
                                    HearingRepository hearingRepository) {
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.dataStoreRepository = dataStoreRepository;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.hearingRepository = hearingRepository;
    }

    @Override
    public void verifyAccess(Long hearingId, List<String> requiredRoles) {
        verifyRoleAccess(requiredRoles, hearingId);
    }

    @Override
    public void verifyCaseAccess(String caseReference, List<String> requiredRoles) {
        verifyCaseAccess(caseReference, requiredRoles, null);
    }

    public void verifyCaseAccess(String caseReference, List<String> requiredRoles, Long hearingId) {
        List<RoleAssignment> filteredRoleAssignments = verifyRoleAccess(requiredRoles, hearingId);

        DataStoreCaseDetails caseDetails = dataStoreRepository.findCaseByCaseIdUsingExternalApi(caseReference);
        if (!checkRoleAssignmentMatchesCaseDetails(caseDetails, filteredRoleAssignments)) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES);
        }
    }

    private List<RoleAssignment> verifyRoleAccess(List<String> requiredRoles, Long hearingId) {
        RoleAssignments roleAssignments = roleAssignmentService.getRoleAssignments(securityUtils.getUserId());
        if (roleAssignments.getRoleAssignments().isEmpty()) {
            throw new ResourceNotFoundException(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, securityUtils.getUserId()));
        }
        List<RoleAssignment> filteredRoleAssignments = filterRoleAssignments(roleAssignments);
        if (filteredRoleAssignments.isEmpty()) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ROLE);
        }

        verifyRequiredRolesExists(requiredRoles, filteredRoleAssignments);
        verifyHearingStatus(filteredRoleAssignments, hearingId);
        return filteredRoleAssignments;
    }

    @Override
    public void verifyHearingCaseAccess(Long hearingId, List<String> requiredRoles) {
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestRepository.getCaseHearing(hearingId);
        if (caseHearingRequestEntity != null) {
            verifyCaseAccess(caseHearingRequestEntity.getCaseReference(), requiredRoles, hearingId);
        }
    }

    private void verifyRequiredRolesExists(List<String> requiredRoles, List<RoleAssignment> filteredRoleAssignments) {
        if (!requiredRoles.isEmpty()) {
            boolean containsRequiredRoles = filteredRoleAssignments.stream()
                .anyMatch(roleAssignment -> requiredRoles.contains(roleAssignment.getRoleName()));

            if (!containsRequiredRoles) {
                throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_MISSING_REQUIRED);
            }
        }
    }

    private void verifyHearingStatus(List<RoleAssignment> filteredRoleAssignments, Long hearingId) {
        if (hearingId != null) {
            String status = hearingRepository.getStatus(hearingId);
            if (filteredRoleAssignments.size() == 1
                && LISTED_HEARING_VIEWER.equals(filteredRoleAssignments.get(0).getRoleName())
                && HearingStatus.LISTED.name().equals(status)) {
                throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_STATUS);
            }
        }
    }

    private List<RoleAssignment> filterRoleAssignments(RoleAssignments roleAssignments) {
        return roleAssignments.getRoleAssignments()
            .stream()
            .filter(roleAssignment -> roleAssignment.getRoleType().equalsIgnoreCase("ORGANISATION")
                && HMC_ROLE_NAMES.contains(roleAssignment.getRoleName()))
            .collect(Collectors.toList());
    }

    private boolean checkRoleAssignmentMatchesCaseDetails(DataStoreCaseDetails caseDetails,
                                                          List<RoleAssignment> roleAssignments) {
        return roleAssignments.stream()
            .anyMatch(roleAssignment -> checkJurisdiction(roleAssignment.getAttributes(), caseDetails.getJurisdiction())
                || checkCaseType(roleAssignment.getAttributes(), caseDetails.getCaseTypeId()));

    }

    @SuppressWarnings("java:S2789")
    private boolean checkJurisdiction(RoleAssignmentAttributes attributes, String jurisdiction) {
        return attributes.getJurisdiction() == null || attributes.getJurisdiction().isEmpty()
            || attributes.getJurisdiction().orElse("").equals(jurisdiction);
    }

    @SuppressWarnings("java:S2789")
    private boolean checkCaseType(RoleAssignmentAttributes attributes, String caseType) {
        return attributes.getCaseType() == null || attributes.getCaseType().isEmpty()
            || attributes.getCaseType().orElse("").equals(caseType);
    }
}
