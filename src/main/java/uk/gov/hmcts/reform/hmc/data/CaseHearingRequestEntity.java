package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "case_hearing_request")
@Entity
@Data
@SecondaryTable(name = "HEARING",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_id")})
@NamedQuery(name = CaseHearingRequestEntity.GET_VERSION_NUMBER_BY_HEARING_ID,
    query = "SELECT versionNumber from CaseHearingRequestEntity where hearing.id = :hearingId")
public class CaseHearingRequestEntity {

    protected static final String GET_VERSION_NUMBER_BY_HEARING_ID = "GET_VERSION_NUMBER";

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
    private Boolean additionalSecurityRequiredFlag;

    @Column(name = "owning_location_id", nullable = false)
    private String owningLocationId;

    @Column(name = "case_restricted_flag", nullable = false)
    private Boolean caseRestrictedFlag;

    @Column(name = "case_sla_start_date", nullable = false)
    private LocalDate caseSlaStartDate;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "interpreter_booking_required_flag")
    private Boolean interpreterBookingRequiredFlag;

    @Column(name = "is_linked_flag")
    private Boolean isLinkedFlag;

    @Column(name = "listing_comments")
    private String listingComments;

    @Column(name = "requester")
    private String requester;

    @Column(name = "hearing_window_start_date_range")
    private LocalDate hearingWindowStartDateRange;

    @Column(name = "hearing_window_end_date_range")
    private LocalDate hearingWindowEndDateRange;

    @Column(name = "request_timestamp", nullable = false)
    private LocalDateTime requestTimeStamp;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id")
    private HearingEntity hearing;

}
