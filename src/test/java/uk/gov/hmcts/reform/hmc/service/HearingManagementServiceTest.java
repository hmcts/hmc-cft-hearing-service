package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_REQUEST_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_WINDOW;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ORG_INDIVIDUAL_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_RELATED_PARTY_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_DOW_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_UNAVAILABILITY_RANGES_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_VERSION_NUMBER;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENTS_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ATTRIBUTES;
import static uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository.ROLE_ASSIGNMENT_INVALID_ROLE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.CASE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class HearingManagementServiceTest {

    @InjectMocks
    private HearingManagementServiceImpl hearingManagementService;

    @Mock
    private DataStoreRepository dataStoreRepository;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private SecurityUtils securityUtils;


    @Mock
    private HearingMapper hearingMapper;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    CaseHearingRequestRepository caseHearingRequestRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingManagementService =
            new HearingManagementServiceImpl(
                roleAssignmentService,
                securityUtils,
                dataStoreRepository,
                hearingRepository,
                hearingMapper,
                caseHearingRequestRepository
            );
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
        HearingEntity hearingEntity = new HearingEntity();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.saveHearingRequest(hearingRequest);
        });
        assertEquals("Hearing window details are required", exception.getMessage());
    }

    @Test
    void shouldFailAsDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.saveHearingRequest(hearingRequest);
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
        given(hearingMapper.modelToEntity(hearingRequest)).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
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
        given(hearingMapper.modelToEntity(hearingRequest)).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());

    }

    @Test
    void shouldPassWithRelatedParty_Details_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(
            TestingUtil.individualWithoutRelatedPartyDetails());
        given(hearingMapper.modelToEntity(hearingRequest)).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());

    }

    @Test
    void shouldPassWithRelatedParty_Details_Not_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        given(hearingMapper.modelToEntity(hearingRequest)).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER, response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());

    }

    @Test
    void shouldFailWithParty_Details_InValid_Org_Individual_details_Present() {
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
            hearingManagementService.saveHearingRequest(hearingRequest);
        });
        assertEquals("Either Individual or Organisation details should be present", exception.getMessage());
    }

    @Test
    void shouldFailWithParty_Details_InValid_Dow_details_Present() {
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
            hearingManagementService.saveHearingRequest(hearingRequest);
        });
        assertEquals("Unavailability DOW details should be present", exception.getMessage());
    }

    @Test
    void shouldFailWithParty_Details_InValid_UnavailabilityRange_details_Present() {
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
            hearingManagementService.saveHearingRequest(hearingRequest);
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

    @Test
    void deleteHearingRequestShouldPassWithValidDetails() {
        when(caseHearingRequestRepository.getVersionNumber(2000000000L)).thenReturn(1);
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        hearingManagementService.deleteHearingRequest(2000000000L, TestingUtil.deleteHearingRequest());
        verify(hearingRepository).existsById(2000000000L);
        verify(caseHearingRequestRepository).getVersionNumber(2000000000L);
    }

    @Test
    void testExpectedException_DeleteHearing_VersionNumber_Not_Equal_To_DB_VersionNumber() {
        when(caseHearingRequestRepository.getVersionNumber(2000000000L)).thenReturn(2);
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(2000000000L, TestingUtil.deleteHearingRequest());
        });
        assertEquals(INVALID_VERSION_NUMBER, exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_NotPresent_inDB() {
        when(hearingRepository.existsById(2000000000L)).thenReturn(false);
        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingManagementService.deleteHearingRequest(2000000000L, TestingUtil.deleteHearingRequest());
        });
        assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_Null() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(null, TestingUtil.deleteHearingRequest());
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_Exceeds_MaxLength() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(20000000001111L, TestingUtil.deleteHearingRequest());
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_First_Char_Is_Not_2() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(1000000100L, TestingUtil.deleteHearingRequest());
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldPassWithValidDetails() {
        when(caseHearingRequestRepository.getVersionNumber(2000000000L)).thenReturn(1);
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        hearingManagementService.updateHearingRequest(2000000000L, TestingUtil.updateHearingRequest());
        verify(hearingRepository).existsById(2000000000L);
        verify(caseHearingRequestRepository).getVersionNumber(2000000000L);
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenVersionNumberDoesNotMatchRequest() {
        when(caseHearingRequestRepository.getVersionNumber(2000000000L)).thenReturn(2);
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, updateHearingRequest));
        assertEquals(INVALID_VERSION_NUMBER, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingRequestDetailsNull() {
        UpdateHearingRequest request = new UpdateHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, request));
        assertEquals(INVALID_HEARING_REQUEST_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingWindowFieldsAreNull() {
        UpdateHearingRequest request = new UpdateHearingRequest();
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        HearingWindow hearingWindow = new HearingWindow();
        hearingDetails.setHearingWindow(hearingWindow);
        request.setHearingDetails(hearingDetails);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, request));
        assertEquals(INVALID_HEARING_WINDOW, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenPartyIndividualAndOrgDetailsNull() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
        hearingDetails.setHearingWindow(hearingWindow);
        PartyDetails partyDetails = new PartyDetails();
        List<PartyDetails> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(partyDetails);
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails);
        request.setPartyDetails(partyDetailsList);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, request));
        assertEquals(INVALID_ORG_INDIVIDUAL_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenPartyIndividualAndOrgDetailsBothExist() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
        hearingDetails.setHearingWindow(hearingWindow);
        PartyDetails partyDetails = new PartyDetails();
        OrganisationDetails organisationDetails = new OrganisationDetails();
        partyDetails.setOrganisationDetails(organisationDetails);
        IndividualDetails individualDetails = new IndividualDetails();
        partyDetails.setIndividualDetails(individualDetails);
        List<PartyDetails> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(partyDetails);
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails);
        request.setPartyDetails(partyDetailsList);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, request));
        assertEquals(INVALID_ORG_INDIVIDUAL_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenPartyUnavailabilityDowIsNotPresent() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
        hearingDetails.setHearingWindow(hearingWindow);
        PartyDetails partyDetails = new PartyDetails();
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail("email");
        partyDetails.setIndividualDetails(individualDetails);
        List<UnavailabilityDow> unavailabilityDowList = new ArrayList<>();
        partyDetails.setUnavailabilityDow(unavailabilityDowList);
        List<PartyDetails> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(partyDetails);
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails);
        request.setPartyDetails(partyDetailsList);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, request));
        assertEquals(INVALID_UNAVAILABILITY_DOW_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenPartyUnavailabilityRangesIsNotPresent() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
        hearingDetails.setHearingWindow(hearingWindow);
        PartyDetails partyDetails = new PartyDetails();
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail("email");
        partyDetails.setIndividualDetails(individualDetails);
        List<UnavailabilityRanges> unavailabilityRanges = new ArrayList<>();
        partyDetails.setUnavailabilityRanges(unavailabilityRanges);
        List<PartyDetails> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(partyDetails);
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails);
        request.setPartyDetails(partyDetailsList);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, request));
        assertEquals(INVALID_UNAVAILABILITY_RANGES_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenRelatedPartyDetailsAreNotPresent() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowEndDateRange(LocalDate.now());
        hearingDetails.setHearingWindow(hearingWindow);
        PartyDetails partyDetails = new PartyDetails();
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setHearingChannelEmail("email");
        List<RelatedParty> relatedParties = new ArrayList<>();
        individualDetails.setRelatedParties(relatedParties);
        partyDetails.setIndividualDetails(individualDetails);
        List<PartyDetails> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(partyDetails);
        UpdateHearingRequest request = new UpdateHearingRequest();
        request.setHearingDetails(hearingDetails);
        request.setPartyDetails(partyDetailsList);
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, request));
        assertEquals(INVALID_RELATED_PARTY_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdIsNull() {
        UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(null, updateHearingRequest));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdExceedsMaxLength() {
        UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(20000000001111L, updateHearingRequest));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdDoesNotStartWith2() {
        UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> hearingManagementService
            .updateHearingRequest(1000000100L, updateHearingRequest));
        assertEquals(INVALID_HEARING_ID_DETAILS, exception.getMessage());
    }

    @Test
    void updateHearingRequestShouldThrowErrorWhenHearingIdNotPresentInDB() {
        when(hearingRepository.existsById(2000000000L)).thenReturn(false);
        UpdateHearingRequest updateHearingRequest = TestingUtil.updateHearingRequest();
        Exception exception = assertThrows(HearingNotFoundException.class, () -> hearingManagementService
            .updateHearingRequest(2000000000L, updateHearingRequest));
        assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
    }

    @Test
    void getHearings_shouldReturnDataWithValidDetails() {
        List<CaseHearingRequestEntity> entities = Arrays.asList(TestingUtil.getCaseHearingsEntities());
        when(caseHearingRequestRepository.getHearingDetailsWithStatus("12345", "HEARING_REQUESTED"))
            .thenReturn(entities);
        GetHearingsResponse response = hearingManagementService.getHearings("12345", "HEARING_REQUESTED");
        assertEquals("12345", response.getCaseRef());
        assertEquals("ABA1", response.getHmctsServiceId());
        assertEquals(1, response.getCaseHearings().size());
        assertEquals(2000000000L, response.getCaseHearings().get(0).getHearingId());
        assertEquals("listingStatus", response.getCaseHearings().get(0).getHearingListingStatus());
        assertEquals("venue1", response.getCaseHearings().get(0)
            .getHearingDaySchedule().get(0).getHearingVenueId());
        assertEquals("SubChannel1", response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getAttendees().get(0).getHearingSubChannel());
        assertEquals(1, response.getCaseHearings().get(0).getHearingDaySchedule().get(0)
            .getHearingJudgeId().size());
    }

    @Test
    void getHearings_shouldReturnNoDataWithStatus_Null() {
        when(caseHearingRequestRepository.getHearingDetails("12345")).thenReturn(new ArrayList<>());
        GetHearingsResponse response = hearingManagementService.getHearings("12345", null);
        assertEquals("12345", response.getCaseRef());
        assertNull(response.getHmctsServiceId());
        assertEquals(0, response.getCaseHearings().size());
    }

    @Test
    void getHearings_shouldReturnNoDataWithInValidStatus() {
        when(caseHearingRequestRepository.getHearingDetailsWithStatus(
            "12345",
            "InvalidStatus"
        )).thenReturn(new ArrayList<>());
        GetHearingsResponse response = hearingManagementService.getHearings("12345", "InvalidStatus");
        assertEquals("12345", response.getCaseRef());
        assertNull(response.getHmctsServiceId());
        assertEquals(0, response.getCaseHearings().size());
    }

    @Test
    void getHearings_shouldReturnNoDataWithBlankStatus() {
        when(caseHearingRequestRepository.getHearingDetails("12345")).thenReturn(new ArrayList<>());
        GetHearingsResponse response = hearingManagementService.getHearings("12345", "");
        assertEquals("12345", response.getCaseRef());
        assertNull(response.getHmctsServiceId());
        assertEquals(0, response.getCaseHearings().size());
    }
}
