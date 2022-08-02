package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "case_hearing_request")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "HEARING",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_id")})
public class CaseHearingRequestEntity extends BaseEntity implements Cloneable, Serializable {

    private static final long serialVersionUID = -3590902739857407292L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "case_hearing_id_seq")
    @Column(name = "case_hearing_id")
    private Long caseHearingID;

    @Column(name = "auto_list_flag", nullable = false)
    private Boolean autoListFlag;

    @Column(name = "listing_auto_change_reason_code", length = 70)
    private String listingAutoChangeReasonCode;

    @Column(name = "hearing_type", nullable = false)
    private String hearingType;

    @Column(name = "required_duration_in_minutes", nullable = false)
    private Integer requiredDurationInMinutes;

    @Column(name = "hearing_priority_type", nullable = false)
    private String hearingPriorityType;

    @Column(name = "number_of_physical_attendees")
    private Integer numberOfPhysicalAttendees;

    @Column(name = "hearing_in_welsh_flag")
    private Boolean hearingInWelshFlag;

    @Column(name = "private_hearing_required_flag")
    private Boolean privateHearingRequiredFlag;

    @Column(name = "lead_judge_contract_type")
    private String leadJudgeContractType;

    @Column(name = "first_date_time_of_hearing_must_be")
    private LocalDateTime firstDateTimeOfHearingMustBe;

    @Column(name = "hmcts_service_code", nullable = false)
    private String hmctsServiceCode;

    @Column(name = "case_reference", nullable = false)
    private String caseReference;

    @Column(name = "external_case_reference")
    private String externalCaseReference;

    @Column(name = "case_url_context_path", nullable = false)
    private String caseUrlContextPath;

    @Column(name = "hmcts_internal_case_name", nullable = false)
    private String hmctsInternalCaseName;

    @Column(name = "public_case_name", nullable = false)
    private String publicCaseName;

    @Column(name = "additional_security_required_flag")
    private Boolean additionalSecurityRequiredFlag;

    @Column(name = "owning_location_id", nullable = false)
    private String owningLocationId;

    @Column(name = "case_restricted_flag", nullable = false)
    private Boolean caseRestrictedFlag;

    @Column(name = "case_sla_start_date", nullable = false)
    private LocalDate caseSlaStartDate;

    @Column(name = "hearing_request_version", nullable = false)
    private Integer versionNumber;

    @Column(name = "interpreter_booking_required_flag")
    private Boolean interpreterBookingRequiredFlag;

    @Column(name = "listing_comments")
    private String listingComments;

    @Column(name = "requester")
    private String requester;

    @Column(name = "hearing_window_start_date_range")
    private LocalDate hearingWindowStartDateRange;

    @Column(name = "hearing_window_end_date_range")
    private LocalDate hearingWindowEndDateRange;

    @Column(name = "hearing_request_received_date_time", nullable = false)
    private LocalDateTime hearingRequestReceivedDateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hearing_id")
    private HearingEntity hearing;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<NonStandardDurationsEntity> nonStandardDurations;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<RequiredLocationsEntity> requiredLocations;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<RequiredFacilitiesEntity> requiredFacilities;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<CaseCategoriesEntity> caseCategories;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<HearingPartyEntity> hearingParties;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PanelRequirementsEntity> panelRequirements;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PanelAuthorisationRequirementsEntity> panelAuthorisationRequirements;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PanelSpecialismsEntity> panelSpecialisms;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PanelUserRequirementsEntity> panelUserRequirements;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<CancellationReasonsEntity> cancellationReasons;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HearingChannelsEntity> hearingChannels;

    @OneToMany(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ChangeReasonsEntity> amendReasonCodes;

    public CaseHearingRequestEntity(CaseHearingRequestEntity original) {
        this.caseHearingID = original.caseHearingID;
        this.autoListFlag = original.autoListFlag;
        this.listingAutoChangeReasonCode = original.listingAutoChangeReasonCode;
        this.hearingType = original.hearingType;
        this.requiredDurationInMinutes = original.requiredDurationInMinutes;
        this.hearingPriorityType = original.hearingPriorityType;
        this.numberOfPhysicalAttendees = original.numberOfPhysicalAttendees;
        this.hearingInWelshFlag = original.hearingInWelshFlag;
        this.privateHearingRequiredFlag = original.privateHearingRequiredFlag;
        this.leadJudgeContractType = original.leadJudgeContractType;
        this.firstDateTimeOfHearingMustBe = original.firstDateTimeOfHearingMustBe;
        this.hmctsServiceCode = original.hmctsServiceCode;
        this.caseReference = original.caseReference;
        this.externalCaseReference = original.externalCaseReference;
        this.caseUrlContextPath = original.caseUrlContextPath;
        this.hmctsInternalCaseName = original.hmctsInternalCaseName;
        this.publicCaseName = original.publicCaseName;
        this.additionalSecurityRequiredFlag = original.additionalSecurityRequiredFlag;
        this.owningLocationId = original.owningLocationId;
        this.caseRestrictedFlag = original.caseRestrictedFlag;
        this.caseSlaStartDate = original.caseSlaStartDate;
        this.versionNumber = original.versionNumber;
        this.interpreterBookingRequiredFlag = original.interpreterBookingRequiredFlag;
        this.listingComments = original.listingComments;
        this.requester = original.requester;
        this.hearingWindowStartDateRange = original.hearingWindowStartDateRange;
        this.hearingWindowEndDateRange = original.hearingWindowEndDateRange;
        this.hearingRequestReceivedDateTime = original.hearingRequestReceivedDateTime;
        this.hearing = original.hearing;
        this.nonStandardDurations = original.nonStandardDurations;
        this.requiredLocations = original.requiredLocations;
        this.requiredFacilities = original.requiredFacilities;
        this.caseCategories = original.caseCategories;
        this.hearingParties = original.hearingParties;
        this.panelRequirements = original.panelRequirements;
        this.panelAuthorisationRequirements = original.panelAuthorisationRequirements;
        this.panelSpecialisms = original.panelSpecialisms;
        this.panelUserRequirements = original.panelUserRequirements;
        this.cancellationReasons = original.cancellationReasons;
        this.hearingChannels = original.hearingChannels;
        this.amendReasonCodes = original.amendReasonCodes;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CaseHearingRequestEntity cloned = (CaseHearingRequestEntity)super.clone();
        cloneCaseCategories(cloned);
        cloneNonStandardDurations(cloned);
        cloneRequiredLocations(cloned);
        cloneRequiredFacilities(cloned);
        cloneHearingParties(cloned);
        clonePanelRequirements(cloned);
        clonePanelAuthorisationRequirements(cloned);
        clonePanelSpecialisms(cloned);
        clonePanelUserRequirements(cloned);
        cloneHearingChannels(cloned);
        cloned.setCaseHearingID(null);
        cloned.setAmendReasonCodes(Collections.emptyList());
        return cloned;
    }

    private void cloneCaseCategories(CaseHearingRequestEntity cloned)  {
        //CaseCategories
        List<CaseCategoriesEntity> caseCategoriesList = new ArrayList<>();
        if (null != cloned.getCaseCategories()) {
            for (CaseCategoriesEntity cce : cloned.getCaseCategories()) {
                CaseCategoriesEntity clonedSubValue = new CaseCategoriesEntity(cce);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                caseCategoriesList.add(clonedSubValue);
            }
        }
        cloned.setCaseCategories(caseCategoriesList);
    }

    private void clonePanelUserRequirements(CaseHearingRequestEntity cloned) {
        //PanelUserRequirementsEntity
        List<PanelUserRequirementsEntity> panelUserRequirementsList = new ArrayList<>();
        if (null != cloned.getPanelUserRequirements()) {
            for (PanelUserRequirementsEntity pure : cloned.getPanelUserRequirements()) {
                PanelUserRequirementsEntity clonedSubValue = new PanelUserRequirementsEntity(pure);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelUserRequirementsList.add(clonedSubValue);
            }
        }
        cloned.setPanelUserRequirements(panelUserRequirementsList);
    }

    private void clonePanelSpecialisms(CaseHearingRequestEntity cloned) {
        //PanelSpecialismsEntity
        List<PanelSpecialismsEntity> panelSpecialismsList = new ArrayList<>();
        if (null != cloned.getPanelSpecialisms()) {
            for (PanelSpecialismsEntity pse : cloned.getPanelSpecialisms()) {
                PanelSpecialismsEntity clonedSubValue = new PanelSpecialismsEntity(pse);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelSpecialismsList.add(clonedSubValue);
            }
        }
        cloned.setPanelSpecialisms(panelSpecialismsList);
    }

    private void clonePanelAuthorisationRequirements(CaseHearingRequestEntity cloned) {
        //PanelAuthorisationRequirementsEntity
        List<PanelAuthorisationRequirementsEntity> panelAuthorisationRequirementsList = new ArrayList<>();
        if (null != cloned.getPanelAuthorisationRequirements()) {
            for (PanelAuthorisationRequirementsEntity par : cloned.getPanelAuthorisationRequirements()) {
                PanelAuthorisationRequirementsEntity clonedSubValue = new PanelAuthorisationRequirementsEntity(par);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelAuthorisationRequirementsList.add(clonedSubValue);
            }
        }
        cloned.setPanelAuthorisationRequirements(panelAuthorisationRequirementsList);
    }

    private void clonePanelRequirements(CaseHearingRequestEntity cloned) {
        //PanelRequirementsEntity
        List<PanelRequirementsEntity> panelRequirementsList = new ArrayList<>();
        if (null != cloned.getPanelRequirements()) {
            for (PanelRequirementsEntity pre : cloned.getPanelRequirements()) {
                PanelRequirementsEntity clonedSubValue = new PanelRequirementsEntity(pre);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelRequirementsList.add(clonedSubValue);
            }
        }
        cloned.setPanelRequirements(panelRequirementsList);
    }

    private void cloneHearingParties(CaseHearingRequestEntity cloned) throws CloneNotSupportedException {
        //HearingPartyEntity
        List<HearingPartyEntity> hearingPartiesList = new ArrayList<>();
        if (null != cloned.getHearingParties()) {
            for (HearingPartyEntity hp : cloned.getHearingParties()) {
                HearingPartyEntity clonedSubValue = (HearingPartyEntity)hp.clone();
                clonedSubValue.setTechPartyId(null);
                clonedSubValue.setCaseHearing(cloned);
                hearingPartiesList.add(clonedSubValue);
            }
        }
        cloned.setHearingParties(hearingPartiesList);

        for (HearingPartyEntity hp : cloned.getHearingParties()) {
            if (!CollectionUtils.isEmpty(hp.getPartyRelationshipDetailsEntity())) {
                for (PartyRelationshipDetailsEntity prde : hp.getPartyRelationshipDetailsEntity()) {
                    prde.setTargetTechParty(getHearingPartyEntityByReference(
                        hp.getPartyReference(),
                        cloned.getHearingParties()
                    ));
                }
            }
        }
    }

    private HearingPartyEntity getHearingPartyEntityByReference(String relatedPartyId,
                                                                List<HearingPartyEntity> hearingPartyEntities) {

        final List<HearingPartyEntity> matchingHearingPartyEntities = hearingPartyEntities.stream()
            .filter(hearingPartyEntity -> relatedPartyId.equals(hearingPartyEntity.getPartyReference()))
            .collect(Collectors.toList());

        if (matchingHearingPartyEntities.size() != 1) {
            throw new BadRequestException(
                String.format("Cannot find unique PartyID with value %s", relatedPartyId));
        }

        return matchingHearingPartyEntities.get(0);
    }

    private void cloneRequiredFacilities(CaseHearingRequestEntity cloned) {
        //RequiredFacilitiesEntity
        List<RequiredFacilitiesEntity> requiredFacilitiesList = new ArrayList<>();
        if (null != cloned.getRequiredFacilities()) {
            for (RequiredFacilitiesEntity rfe : cloned.getRequiredFacilities()) {
                RequiredFacilitiesEntity clonedSubValue =  new RequiredFacilitiesEntity(rfe);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                requiredFacilitiesList.add(clonedSubValue);
            }
        }
        cloned.setRequiredFacilities(requiredFacilitiesList);
    }

    private void cloneRequiredLocations(CaseHearingRequestEntity cloned) {
        //RequiredLocationsEntity
        List<RequiredLocationsEntity> requiredLocationsList = new ArrayList<>();
        if (null != cloned.getRequiredLocations()) {
            for (RequiredLocationsEntity rle : cloned.getRequiredLocations()) {
                RequiredLocationsEntity clonedSubValue = new RequiredLocationsEntity(rle);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                requiredLocationsList.add(clonedSubValue);
            }
        }
        cloned.setRequiredLocations(requiredLocationsList);
    }

    private void cloneNonStandardDurations(CaseHearingRequestEntity cloned) {
        //nonStandardDuration
        List<NonStandardDurationsEntity> nonStandardDurationsList = new ArrayList<>();
        if (null != cloned.getNonStandardDurations()) {
            for (NonStandardDurationsEntity nsde : cloned.getNonStandardDurations()) {
                NonStandardDurationsEntity clonedSubValue = new NonStandardDurationsEntity(nsde);
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                nonStandardDurationsList.add(clonedSubValue);
            }
        }
        cloned.setNonStandardDurations(nonStandardDurationsList);
    }

    private void cloneHearingChannels(CaseHearingRequestEntity cloned) {
        List<HearingChannelsEntity> hearingChannelsList = new ArrayList<>();
        if (null != cloned.getHearingChannels()) {
            for (HearingChannelsEntity hce : cloned.getHearingChannels()) {
                HearingChannelsEntity clonedSubValue = new HearingChannelsEntity(hce);
                clonedSubValue.setHearingChannelsId(null);
                clonedSubValue.setCaseHearing(cloned);
                hearingChannelsList.add(clonedSubValue);
            }
        }
        cloned.setHearingChannels(hearingChannelsList);
    }
}
