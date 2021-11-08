package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "case_hearing_request")
@Entity
@Data
public class CaseHearingRequestEntity {

    @Id
    @Column(name = "case_hearing_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long caseHearingID;

    @Column(name = "auto_list_flag", nullable = false)
    private boolean autoListFlag;

    @Column(name = "hearing_type", nullable = false)
    private String hearingType;

    @Column(name = "required_duration_in_minutes", nullable = false)
    private Integer requiredDurationInMinutes;

    @Column(name = "hearing_priority_type", nullable = false)
    private String hearingPriorityType;

    @Column(name = "number_of_physical_attendees")
    private Integer numberOfPhysicalAttendees;

    @Column(name = "hearing_in_welsh_flag")
    private boolean hearingInWelshFlag;

    @Column(name = "private_hearing_required_flag")
    private boolean privateHearingRequiredFlag;

    @Column(name = "lead_judge_contract_type")
    private String leadJudgeContractType;

    @Column(name = "first_date_time_of_hearing_must_be")
    private LocalDateTime firstDateTimeOfHearingMustBe;

    @Column(name = "hmcts_service_id", nullable = false)
    private String hmctsServiceID;

    @Column(name = "case_reference", nullable = false)
    private String caseReference;

    @Column(name = "hearing_request_received_date_time", nullable = false)
    private LocalDateTime hearingRequestReceivedDateTime;

    @Column(name = "external_case_reference")
    private String externalCaseReference;

    @Column(name = "case_url_context_path", nullable = false)
    private String caseUrlContextPath;

    @Column(name = "hmcts_internal_case_name", nullable = false)
    private String hmctsInternalCaseName;

    @Column(name = "public_case_name", nullable = false)
    private String publicCaseName;

    @Column(name = "additional_security_required_flag")
    private boolean additionalSecurityRequiredFlag;

    @Column(name = "owning_location_id", nullable = false)
    private String owningLocationId;

    @Column(name = "case_restricted_flag", nullable = false)
    private boolean caseRestrictedFlag;

    @Column(name = "case_sla_start_date", nullable = false)
    private LocalDateTime caseSlaStartDate;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "hearing_id", nullable = false)
    private Long hearingId;

    @Column(name = "interpreter_booking_required_flag")
    private boolean interpreterBookingRequiredFlag;

    @Column(name = "is_linked_flag")
    private boolean isLinkedFlag;

    @Column(name = "listing_comments")
    private String listingComments;

    @Column(name = "requester")
    private String requester;

    @Column(name = "hearing_window_start_date_range")
    private LocalDateTime hearingWindowStartDateRange;

    @Column(name = "hearing_window_end_date_range")
    private LocalDateTime hearingWindowEndDateRange;

    @Column(name = "request_timestamp", nullable = false)
    private LocalDateTime requestTimeStamp;

    @OneToOne(mappedBy = "caseHearing")
    private CaseCategoriesEntity caseCategoriesEntity;

}
