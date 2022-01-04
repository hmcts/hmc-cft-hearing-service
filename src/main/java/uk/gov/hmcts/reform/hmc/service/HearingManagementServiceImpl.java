package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.hmc.domain.model.RoleAssignments;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.InvalidRoleAssignmentException;
import uk.gov.hmcts.reform.hmc.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.model.Attendees;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.DataStoreRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
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

@Service
@Component
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private final DataStoreRepository dataStoreRepository;
    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;
    private HearingRepository hearingRepository;
    private final HearingMapper hearingMapper;

    private CaseHearingRequestRepository caseHearingRequestRepository;

    @Autowired
    public HearingManagementServiceImpl(RoleAssignmentService roleAssignmentService, SecurityUtils securityUtils,
                                        @Qualifier("defaultDataStoreRepository")
                                            DataStoreRepository dataStoreRepository,
                                        HearingRepository hearingRepository,
                                        HearingMapper hearingMapper,
                                        CaseHearingRequestRepository caseHearingRequestRepository) {
        this.dataStoreRepository = dataStoreRepository;
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
        this.hearingRepository = hearingRepository;
        this.hearingMapper = hearingMapper;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
    }

    @Override
    public void getHearingRequest(Long hearingId, boolean isValid) {
        if (isValid && !hearingRepository.existsById(hearingId)) {
            throw new HearingNotFoundException(hearingId);
        }
    }

    @Override
    @Transactional
    public HearingResponse saveHearingRequest(HearingRequest hearingRequest) {
        if (hearingRequest == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
        validateHearingRequest(hearingRequest);
        return insertHearingRequest(hearingRequest);
    }

    @Override
    public void updateHearingRequest(Long hearingId, UpdateHearingRequest hearingRequest) {
        validateHearingRequest(hearingRequest);
        validateHearingId(hearingId);
        validateVersionNumber(hearingId, hearingRequest.getRequestDetails().getVersionNumber());
    }

    /**
     * validate Get Hearing Request by caseRefId or caseRefId/caseStatus.
     * @param caseRef case Ref
     * @param status status
     * @return HearingRequest HearingRequest
     */
    @Override
    public GetHearingsResponse getHearings(String caseRef, String status) {
        log.info("caseRef:{} ; status:{}", caseRef, status);
        List<CaseHearingRequestEntity> entities;
        if (!isBlank(status)) {
            entities = caseHearingRequestRepository.getHearingDetailsWithStatus(caseRef, status);
        } else {
            entities = caseHearingRequestRepository.getHearingDetails(caseRef);
        }
        return getHearingsResponseDetails(caseRef,entities);
    }

    private HearingResponse insertHearingRequest(HearingRequest hearingRequest) {
        HearingEntity savedEntity = saveHearingDetails(hearingRequest);
        return getSaveHearingResponseDetails(savedEntity);
    }

    private HearingEntity saveHearingDetails(HearingRequest hearingRequest) {
        HearingEntity hearingEntity = hearingMapper.modelToEntity(hearingRequest);
        return hearingRepository.save(hearingEntity);
    }

    private HearingResponse getSaveHearingResponseDetails(HearingEntity savedEntity) {
        log.info("Hearing details saved successfully with id: {}", savedEntity.getId());
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingRequestId(savedEntity.getId());
        hearingResponse.setTimeStamp(savedEntity.getCaseHearingRequest().getHearingRequestReceivedDateTime());
        hearingResponse.setStatus(savedEntity.getStatus());
        hearingResponse.setVersionNumber(savedEntity.getCaseHearingRequest().getVersionNumber());
        return hearingResponse;
    }

    private void validateHearingRequest(HearingRequest hearingRequest) {
        validateHearingRequestDetails(hearingRequest);
        validateHearingDetails(hearingRequest.getHearingDetails());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
        }
    }

    private void validateHearingRequest(UpdateHearingRequest hearingRequest) {
        validateHearingRequestDetails(hearingRequest);
        validateHearingDetails(hearingRequest.getHearingDetails());
        if (hearingRequest.getPartyDetails() != null) {
            validatePartyDetails(hearingRequest.getPartyDetails());
        }
    }

    private GetHearingsResponse getHearingsResponseDetails(String caseRef, List<CaseHearingRequestEntity> entities) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        if (!entities.isEmpty()) {
            getHearingsResponse.setCaseRef(entities.get(0).getCaseReference());
            getHearingsResponse.setHmctsServiceId(entities.get(0).getHmctsServiceID());
            setCaseHearings(entities, getHearingsResponse);
        } else {
            getHearingsResponse.setCaseRef(caseRef);
            getHearingsResponse.setCaseHearings(new ArrayList<>());
        }
        return getHearingsResponse;
    }

    private void setCaseHearings(List<CaseHearingRequestEntity> entities, GetHearingsResponse getHearingsResponse) {
        List<CaseHearing> caseHearingList = new ArrayList<>();
        for (CaseHearingRequestEntity entity : entities) {
            CaseHearing caseHearing = getCaseHearing(entity);
            List<HearingResponseEntity> hearingResponses = getHearingResponseEntities(entity, caseHearing);
            setHearingDaySchedule(caseHearingList, caseHearing, hearingResponses);
        }
        getHearingsResponse.setCaseHearings(caseHearingList);
    }

    private void setHearingDaySchedule(List<CaseHearing> caseHearingList, CaseHearing caseHearing,
                                       List<HearingResponseEntity> hearingResponses) {
        List<HearingDaySchedule> scheduleList = new ArrayList<>();

        for (HearingResponseEntity hearingResponseEntity : hearingResponses) {
            List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponseEntity.getHearingDayDetails();
            for (HearingDayDetailsEntity detailEntity : hearingDayDetailEntities) {
                HearingDaySchedule hearingDaySchedule = setHearingDayScheduleDetails(detailEntity);
                setHearingJudgeIds(detailEntity.getHearingDayPanel(), hearingDaySchedule);
                setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                scheduleList.add(hearingDaySchedule);
            }
            caseHearing.setHearingDaySchedule(scheduleList);
            caseHearingList.add(caseHearing);
        }
    }

    private List<HearingResponseEntity> getHearingResponseEntities(CaseHearingRequestEntity entity,
                                                                   CaseHearing caseHearing) {
        List<HearingResponseEntity> hearingResponses = entity.getHearing().getHearingResponse();
        setHearingResponseDetails(caseHearing, hearingResponses);
        return hearingResponses;
    }

    private CaseHearing getCaseHearing(CaseHearingRequestEntity entity) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(entity.getHearing().getId());
        caseHearing.setHearingRequestDateTime(entity.getHearingRequestReceivedDateTime());
        caseHearing.setHearingType(entity.getHearingType());
        caseHearing.setHmcStatus(entity.getHearing().getStatus());
        return caseHearing;
    }

    private void setHearingResponseDetails(CaseHearing caseHearing, List<HearingResponseEntity> entities) {
        for (HearingResponseEntity hearingResponseEntity : entities) {
            caseHearing.setLastResponseReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
            caseHearing.setResponseVersion(hearingResponseEntity.getHearingResponseId());
            caseHearing.setHearingListingStatus(hearingResponseEntity.getListingStatus());
            caseHearing.setListAssistCaseStatus(hearingResponseEntity.getListingCaseStatus());
        }
    }

    private HearingDaySchedule setHearingDayScheduleDetails(HearingDayDetailsEntity detailEntity) {
        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        hearingDaySchedule.setHearingStartDateTime(detailEntity.getStartDateTime());
        hearingDaySchedule.setHearingEndDateTime(detailEntity.getEndDateTime());
        hearingDaySchedule.setListAssistSessionId(detailEntity.getListAssistSessionId());
        hearingDaySchedule.setHearingVenueId(detailEntity.getVenueId());
        hearingDaySchedule.setHearingRoomId(detailEntity.getRoomId());
        return hearingDaySchedule;
    }

    private void setAttendeeDetails(List<HearingAttendeeDetailsEntity> attendeeDetailsEntities,
                                    HearingDaySchedule hearingDaySchedule) {
        List<Attendees> attendeesList = new ArrayList<>();
        for (HearingAttendeeDetailsEntity attendeeDetailEntity : attendeeDetailsEntities) {
            Attendees attendee = new Attendees();
            attendee.setPartyId(attendeeDetailEntity.getPartyId());
            attendee.setHearingSubChannel(attendeeDetailEntity.getPartySubChannelType());
            attendeesList.add(attendee);
        }
        hearingDaySchedule.setAttendees(attendeesList);
    }

    private void setHearingJudgeIds(List<HearingDayPanelEntity> hearingDayPanelEntities,
                                    HearingDaySchedule hearingDaySchedule) {
        List<String> hearingDayPanelList = new ArrayList<>();
        for (HearingDayPanelEntity dayPanelEntity : hearingDayPanelEntities) {
            hearingDayPanelList.add(dayPanelEntity.getPanelUserId());
        }
        hearingDaySchedule.setHearingJudgeId(hearingDayPanelList);
    }

    private void validatePartyDetails(List<PartyDetails> partyDetails) {
        for (PartyDetails partyDetail : partyDetails) {
            if ((partyDetail.getIndividualDetails() != null && partyDetail.getOrganisationDetails() != null)
                || (partyDetail.getIndividualDetails() == null && partyDetail.getOrganisationDetails() == null)) {
                throw new BadRequestException(INVALID_ORG_INDIVIDUAL_DETAILS);
            }
            if (partyDetail.getUnavailabilityDow() != null && partyDetail.getUnavailabilityDow().isEmpty()) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_DOW_DETAILS);
            }
            if (partyDetail.getUnavailabilityRanges() != null && partyDetail.getUnavailabilityRanges().isEmpty()) {
                throw new BadRequestException(INVALID_UNAVAILABILITY_RANGES_DETAILS);
            }
            if (partyDetail.getIndividualDetails() != null
                && (partyDetail.getIndividualDetails().getRelatedParties() != null
                && partyDetail.getIndividualDetails().getRelatedParties().isEmpty())) {
                throw new BadRequestException(INVALID_RELATED_PARTY_DETAILS);
            }
        }
    }

    private void validateHearingRequestDetails(HearingRequest hearingRequest) {
        if (hearingRequest.getRequestDetails() == null
            && hearingRequest.getHearingDetails() == null
            && hearingRequest.getCaseDetails() == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
    }

    private void validateHearingRequestDetails(UpdateHearingRequest hearingRequest) {
        if (hearingRequest.getRequestDetails() == null
            && hearingRequest.getHearingDetails() == null
            && hearingRequest.getCaseDetails() == null) {
            throw new BadRequestException(INVALID_HEARING_REQUEST_DETAILS);
        }
    }

    private void validateHearingDetails(HearingDetails hearingDetails) {
        if (hearingDetails.getHearingWindow().getHearingWindowEndDateRange() == null
            && hearingDetails.getHearingWindow().getHearingWindowStartDateRange() == null
            && hearingDetails.getHearingWindow().getFirstDateTimeMustBe() == null) {
            throw new BadRequestException(INVALID_HEARING_WINDOW);
        }
    }

    public void verifyAccess(String caseReference) {
        RoleAssignments roleAssignments = roleAssignmentService.getRoleAssignments(securityUtils.getUserId());
        if (roleAssignments.getRoleAssignments().isEmpty()) {
            throw new ResourceNotFoundException(String.format(ROLE_ASSIGNMENTS_NOT_FOUND, securityUtils.getUserId()));
        }
        List<RoleAssignment> filteredRoleAssignments = new ArrayList<>();
        for (RoleAssignment roleAssignment : roleAssignments.getRoleAssignments()) {
            if (roleAssignment.getRoleName().equalsIgnoreCase("Hearing Manage")
                && roleAssignment.getRoleType().equalsIgnoreCase(
                "ORGANISATION")) {
                filteredRoleAssignments.add(roleAssignment);
            }
        }
        if (filteredRoleAssignments.isEmpty()) {
            throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ROLE);
        } else {
            DataStoreCaseDetails caseDetails;
            caseDetails = dataStoreRepository.findCaseByCaseIdUsingExternalApi(caseReference);
            if (!checkRoleAssignmentMatchesCaseDetails(caseDetails, filteredRoleAssignments)) {
                throw new InvalidRoleAssignmentException(ROLE_ASSIGNMENT_INVALID_ATTRIBUTES);
            }
        }
    }

    @SuppressWarnings("java:S2789")
    private boolean checkRoleAssignmentMatchesCaseDetails(DataStoreCaseDetails caseDetails,
                                                          List<RoleAssignment> roleAssignments) {
        for (RoleAssignment roleAssignment : roleAssignments) {
            RoleAssignmentAttributes attributes = roleAssignment.getAttributes();
            if (attributes.getJurisdiction() == null) {
                return ifJurisdictionIsNullOrEmpty(attributes, caseDetails);
            } else if (attributes.getJurisdiction().isEmpty()) {
                return ifJurisdictionIsNullOrEmpty(attributes, caseDetails);
            } else if (attributes.getJurisdiction().equals(Optional.of(caseDetails.getJurisdiction()))) {
                return true;
            } else if (attributes.getCaseType() != null && attributes.getCaseType().isPresent() && attributes
                .getCaseType().equals(Optional.of(caseDetails.getCaseTypeId()))) {
                return true;
            }

        }
        return false;
    }

    @SuppressWarnings("java:S2789")
    private boolean ifJurisdictionIsNullOrEmpty(RoleAssignmentAttributes attributes, DataStoreCaseDetails caseDetails) {
        if (attributes.getCaseType() == null) {
            return true;
        } else if (attributes.getCaseType().isEmpty()) {
            return true;
        } else {
            return attributes.getCaseType().equals(Optional.of(caseDetails.getCaseTypeId()));
        }
    }


    @Override
    public void deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest) {
        validateHearingId(hearingId);
        validateVersionNumber(hearingId, deleteRequest.getVersionNumber());
    }

    private void validateVersionNumber(Long hearingId, Integer versionNumber) {
        Integer versionNumberFromDb = getVersionNumber(hearingId);
        if (!versionNumberFromDb.equals(versionNumber)) {
            throw new BadRequestException(INVALID_VERSION_NUMBER);
        }
    }

    private Integer getVersionNumber(Long hearingId) {
        return caseHearingRequestRepository.getVersionNumber(hearingId);
    }

    private void validateHearingId(Long hearingId) {
        if (hearingId == null) {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        } else {
            String hearingIdStr = String.valueOf(hearingId);
            isValidFormat(hearingIdStr);
            if (!hearingRepository.existsById(hearingId)) {
                throw new HearingNotFoundException(hearingId);
            }
        }
    }

    private void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
            || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }
}
