package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;

@Service
@Slf4j
public class AccessControlServiceImpl implements AccessControlService {

    private RoleAssignmentService roleAssignmentService;
    private SecurityUtils securityUtils;
    private DataStoreRepository dataStoreRepository;

    private static final List<String> HMC_ROLE_NAMES = Lists.newArrayList("hearing-manager",
                                                                          "hearing-viewer",
                                                                          "listed-hearing-viewer");

    public AccessControlServiceImpl(RoleAssignmentService roleAssignmentService,
                                    SecurityUtils securityUtils,
                                    @Qualifier("defaultDataStoreRepository")
                                        DataStoreRepository dataStoreRepository) {
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.dataStoreRepository = dataStoreRepository;
    }


    @Override
    public void verifyCaseAccess(String caseReference) {
        RoleAssignments roleAssignments = roleAssignmentService.getRoleAssignments(securityUtils.getUserId());
        if (roleAssignments.getRoleAssignments().isEmpty()) {
            throw new ResourceNotFoundException(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, securityUtils.getUserId()));
        }
        List<RoleAssignment> filteredRoleAssignments = filterRoleAssignments(roleAssignments);
        if (filteredRoleAssignments.isEmpty()) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ROLE);
        }
        DataStoreCaseDetails caseDetails = dataStoreRepository.findCaseByCaseIdUsingExternalApi(caseReference);
        if (!checkRoleAssignmentMatchesCaseDetails(caseDetails, filteredRoleAssignments)) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES);
        }
    }

    private List<RoleAssignment> filterRoleAssignments(RoleAssignments roleAssignments) {
        return roleAssignments.getRoleAssignments()
            .stream()
            .filter(roleAssignment -> roleAssignment.getRoleType().equalsIgnoreCase("ORGANISATION")
                && HMC_ROLE_NAMES.contains(roleAssignment.getRoleName()))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("java:S2789")
    private boolean checkRoleAssignmentMatchesCaseDetails(DataStoreCaseDetails caseDetails,
                                                          List<RoleAssignment> roleAssignments) {
        return roleAssignments.stream()
            .anyMatch(roleAssignment -> checkJurisdiction(roleAssignment.getAttributes(), caseDetails.getJurisdiction())
                || checkCaseType(roleAssignment.getAttributes(), caseDetails.getCaseTypeId()));

    }

    private boolean checkJurisdiction(RoleAssignmentAttributes attributes, String jurisdiction) {
        return attributes.getJurisdiction() == null || attributes.getJurisdiction().isEmpty()
            || attributes.getJurisdiction().get().equals(Optional.of(jurisdiction));
    }

    private boolean checkCaseType(RoleAssignmentAttributes attributes, String caseType) {
        return attributes.getCaseType() == null || attributes.getCaseType().isEmpty()
            || attributes.getCaseType().get().equals(caseType);
    }
}
