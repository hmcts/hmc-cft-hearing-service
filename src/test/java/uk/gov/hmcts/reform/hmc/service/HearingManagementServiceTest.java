package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class HearingManagementServiceTest {

    @Mock
    private DataStoreRepository dataStoreRepository;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private SecurityUtils securityUtils;

    private HearingManagementServiceImpl hearingManagementService;

    @Mock
    HearingRepository hearingRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        hearingManagementService = new HearingManagementServiceImpl(hearingRepository);
    }

    public static final String JURISDICTION = "Jurisdiction1";
    public static final String CASE_TYPE = "CaseType1";
    public static final String USER_ID = "UserId";
    public static final String ROLE_NAME = "Hearing Manage";
    public static final String ROLE_TYPE = "ORGANISATION";


    @Test
    void shouldFailWithInvalidHearingId() {
        HearingEntity hearing = new HearingEntity();
        hearing.setStatus("RESPONDED");
        hearing.setId(1L);

        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingManagementService.getHearingRequest(1L, true);
        });
        assertEquals("No hearing found for reference: 1", exception.getMessage());
    }

    @Test
    void shouldPassWithValidHearingId() {
        HearingEntity hearing = new HearingEntity();
        hearing.setStatus("RESPONDED");
        hearing.setId(1L);
        when(hearingRepository.existsById(1L)).thenReturn(true);
        hearingManagementService.getHearingRequest(1L, true);
        verify(hearingRepository).existsById(1L);
    }

    @Test
    void shouldFailAsHearingWindowDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.getHearingDetails().getHearingWindow().setHearingWindowStartDateRange(null);
        hearingRequest.getHearingDetails().getHearingWindow().setHearingWindowEndDateRange(null);
        hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Hearing window details are required", exception.getMessage());
    }

    @Test
    void shouldFailAsDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Invalid details", exception.getMessage());
    }

    @Test
    void shouldPassWithHearing_Case_Request_Details_Valid() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingManagementService.validateHearingRequest(hearingRequest);
    }

    @Test
    void shouldPassWithHearing_Case_Request_Party_Details_Valid() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        hearingManagementService.validateHearingRequest(hearingRequest);
    }

    @Test
    void shouldPassWithParty_Details_InValid_Org_Individual_details_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        List<PartyDetails> partyDetails = TestingUtil.partyDetails();
        partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
        partyDetails.get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.setPartyDetails(partyDetails);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Either Individual or Organisation details should be present", exception.getMessage());
    }

    @Test
    void shouldPassWithParty_Details_InValid_Dow_details_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        List<PartyDetails> partyDetails = TestingUtil.partyDetails();
        partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
        partyDetails.get(0).setUnavailabilityDow(new ArrayList<>());
        hearingRequest.setPartyDetails(partyDetails);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Unavailability DOW details should be present", exception.getMessage());
    }

    @Test
    void shouldPassWithParty_Details_InValid_UnavailabilityRange_details_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        List<PartyDetails> partyDetails = TestingUtil.partyDetails();
        partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
        partyDetails.get(0).setUnavailabilityRanges(new ArrayList<>());
        hearingRequest.setPartyDetails(partyDetails);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Unavailability range details should be present", exception.getMessage());

    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesCaseJurisdictionAndCaseTypeId() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .caseType(Optional.of(CASE_TYPE))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesOnlyCaseJurisdiction() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .caseType(Optional.of(CASE_TYPE))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId("different casetypeid")
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndMatchesOnlyCaseTypeId() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .caseType(Optional.of(CASE_TYPE))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction("different Jurisdiction")
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndNoJurisdictionOrCaseTypeIdGivenInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionAndCaseTypeIdIsEmptyInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.empty())
            .caseType(Optional.empty())
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction("different Jurisdiction")
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdMatchesAndJurisdictionIsEmptyInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.empty())
            .caseType(Optional.of(CASE_TYPE))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionMatchesAndCaseTypeIdIsEmptyInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .caseType(Optional.empty())
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndJurisdictionMatchesAndCaseTypeIdIsNullInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdMatchesAndJurisdictionIsNullInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .caseType(Optional.of(CASE_TYPE))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdIsEmptyAndJurisdictionIsNullInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .caseType(Optional.empty())
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldVerifyAccessWhenRoleAssignmentValidAndCaseTypeIdIsNullAndJurisdictionIsEmptyInRoleAssignment() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.empty())
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        hearingManagementService.verifyAccess(CASE_REFERENCE);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenNoRoleAssignmentsReturned() {
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        });
        assertEquals(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, USER_ID), exception.getMessage());
    }

    @Test
    void shouldThrowInvalidRoleAssignmentExceptionWhenFilteredRoleAssignmentsListEmpty() {
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName("divorce")
            .roleType(ROLE_TYPE)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> {
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        });
        assertEquals(ROLE_ASSIGNMENT_INVALID_ROLE, exception.getMessage());
    }

    @Test
    void shouldThrowInvalidRoleAssignmentExceptionWhenRoleAssignmentDoesNotMatchCaseDetails() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .caseType(Optional.of(CASE_TYPE))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction("Different Jurisdiction")
            .caseTypeId("Different CaseTypeId")
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> {
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        });
        assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
    }

    @Test
    void shouldThrowInvalidRoleAssignmentExceptionWhenJurisdictionIsInvalidAndCaseTypeIsNull() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction("Different Jurisdiction")
            .caseTypeId("Different CaseTypeId")
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> {
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        });
        assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
    }

    @Test
    void shouldThrowInvalidRoleAssignmentExceptionWhenJurisdictionIsInvalidAndCaseTypeIsEmpty() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .jurisdiction(Optional.of(JURISDICTION))
            .caseType(Optional.empty())
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction("Different Jurisdiction")
            .caseTypeId("Different CaseTypeId")
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> {
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        });
        assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
    }

    @Test
    void shouldThrowInvalidRoleAssignmentExceptionWhenCaseTypeIsInvalidAndJurisdictionIsNull() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .caseType(Optional.of(CASE_TYPE))
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction("Different Jurisdiction")
            .caseTypeId("Different CaseTypeId")
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> {
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        });
        assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
    }

    @Test
    void shouldThrowInvalidRoleAssignmentExceptionWhenCaseTypeIsInvalidAndJurisdictionIsEmpty() {
        RoleAssignmentAttributes roleAssignmentAttributes = RoleAssignmentAttributes.builder()
            .caseType(Optional.of(CASE_TYPE))
            .jurisdiction(Optional.empty())
            .build();
        RoleAssignment roleAssignment = RoleAssignment.builder()
            .roleName(ROLE_NAME)
            .roleType(ROLE_TYPE)
            .attributes(roleAssignmentAttributes)
            .build();
        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        roleAssignmentList.add(roleAssignment);
        RoleAssignments roleAssignments = RoleAssignments.builder()
            .roleAssignments(roleAssignmentList)
            .build();
        doReturn(roleAssignments).when(roleAssignmentService).getRoleAssignments(USER_ID);
        doReturn(USER_ID).when(securityUtils).getUserId();
        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .jurisdiction("Different Jurisdiction")
            .caseTypeId("Different CaseTypeId")
            .build();
        doReturn(caseDetails).when(dataStoreRepository).findCaseByCaseIdUsingExternalApi(CASE_REFERENCE);
        Exception exception = assertThrows(InvalidRoleAssignmentException.class, () -> {
            hearingManagementService.verifyAccess(CASE_REFERENCE);
        });
        assertEquals(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES, exception.getMessage());
    }
}
