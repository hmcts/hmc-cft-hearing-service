package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "case_hearing_request")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SecondaryTable(name = "HEARING",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_id")})
public class CaseHearingRequestEntity extends BaseEntity implements Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "case_hearing_id_seq")
    @Column(name = "case_hearing_id")
    private Long caseHearingID;

    @Column(name = "auto_list_flag", nullable = false)
    private Boolean autoListFlag;

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

    @Column(name = "amend_reason_code")
    private String amendReasonCode;

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

    @OneToOne(mappedBy = "caseHearing", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private CancellationReasonsEntity cancellationReason;

    @Override
    public Object clone() throws CloneNotSupportedException {
        CaseHearingRequestEntity cloned = (CaseHearingRequestEntity)super.clone();
        //CaseCategories
        List<CaseCategoriesEntity> caseCategories = new ArrayList<>();
        if (null != cloned.getCaseCategories()) {
            for (CaseCategoriesEntity cce : cloned.getCaseCategories()) {
                CaseCategoriesEntity clonedSubValue = (CaseCategoriesEntity)cce.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                caseCategories.add(clonedSubValue);
            }
        }
        cloned.setCaseCategories(caseCategories);

        //nonStandardDuration
        List<NonStandardDurationsEntity> nonStandardDurations = new ArrayList<>();
        if (null != cloned.getNonStandardDurations()) {
            for (NonStandardDurationsEntity nsde : cloned.getNonStandardDurations()) {
                NonStandardDurationsEntity clonedSubValue = (NonStandardDurationsEntity)nsde.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                nonStandardDurations.add(clonedSubValue);
            }
        }
        cloned.setNonStandardDurations(nonStandardDurations);

        //RequiredLocationsEntity
        List<RequiredLocationsEntity> requiredLocations = new ArrayList<>();
        if (null != cloned.getRequiredLocations()) {
            for (RequiredLocationsEntity rle : cloned.getRequiredLocations()) {
                RequiredLocationsEntity clonedSubValue = (RequiredLocationsEntity)rle.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                requiredLocations.add(clonedSubValue);
            }
        }
        cloned.setRequiredLocations(requiredLocations);

        //RequiredFacilitiesEntity
        List<RequiredFacilitiesEntity> requiredFacilities = new ArrayList<>();
        if (null != cloned.getRequiredFacilities()) {
            for (RequiredFacilitiesEntity rfe : cloned.getRequiredFacilities()) {
                RequiredFacilitiesEntity clonedSubValue = (RequiredFacilitiesEntity)rfe.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                requiredFacilities.add(clonedSubValue);
            }
        }
        cloned.setRequiredFacilities(requiredFacilities);

        //HearingPartyEntity
        List<HearingPartyEntity> hearingParties = new ArrayList<>();
        if (null != cloned.getHearingParties()) {
            for (HearingPartyEntity hp : cloned.getHearingParties()) {
                HearingPartyEntity clonedSubValue = (HearingPartyEntity)hp.clone();
                clonedSubValue.setTechPartyId(null);
                clonedSubValue.setCaseHearing(cloned);
                hearingParties.add(clonedSubValue);
            }
        }
        cloned.setHearingParties(hearingParties);

        //PanelRequirementsEntity
        List<PanelRequirementsEntity> panelRequirements = new ArrayList<>();
        if (null != cloned.getPanelRequirements()) {
            for (PanelRequirementsEntity pre : cloned.getPanelRequirements()) {
                PanelRequirementsEntity clonedSubValue = (PanelRequirementsEntity)pre.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelRequirements.add(clonedSubValue);
            }
        }
        cloned.setPanelRequirements(panelRequirements);

        //PanelAuthorisationRequirementsEntity
        List<PanelAuthorisationRequirementsEntity> panelAuthorisationRequirements = new ArrayList<>();
        if (null != cloned.getPanelAuthorisationRequirements()) {
            for (PanelAuthorisationRequirementsEntity par : cloned.getPanelAuthorisationRequirements()) {
                PanelAuthorisationRequirementsEntity clonedSubValue = (PanelAuthorisationRequirementsEntity)par.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelAuthorisationRequirements.add(clonedSubValue);
            }
        }
        cloned.setPanelAuthorisationRequirements(panelAuthorisationRequirements);

        //PanelSpecialismsEntity
        List<PanelSpecialismsEntity> panelSpecialisms = new ArrayList<>();
        if (null != cloned.getPanelSpecialisms()) {
            for (PanelSpecialismsEntity pse : cloned.getPanelSpecialisms()) {
                PanelSpecialismsEntity clonedSubValue = (PanelSpecialismsEntity)pse.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelSpecialisms.add(clonedSubValue);
            }
        }
        cloned.setPanelSpecialisms(panelSpecialisms);

        //PanelUserRequirementsEntity
        List<PanelUserRequirementsEntity> panelUserRequirements = new ArrayList<>();
        if (null != cloned.getPanelUserRequirements()) {
            for (PanelUserRequirementsEntity pure : cloned.getPanelUserRequirements()) {
                PanelUserRequirementsEntity clonedSubValue = (PanelUserRequirementsEntity)pure.clone();
                clonedSubValue.setId(null);
                clonedSubValue.setCaseHearing(cloned);
                panelUserRequirements.add(clonedSubValue);
            }
        }
        cloned.setPanelUserRequirements(panelUserRequirements);
        cloned.setCaseHearingID(null);
        return cloned;
    }
}
