package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "individual_detail")
@Entity
@Data
@SecondaryTable(name = "hearing_party",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "TECH_PARTY_ID")})
public class IndividualDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "individual_detail_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "vulnerability_details")
    private String vulnerabilityDetails;

    @Column(name = "vulnerable_flag")
    private Boolean vulnerableFlag;

    @Column(name = "interpreter_language")
    private String interpreterLanguage;

    @Column(name = "channel_type")
    private String channelType;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    @Column(name = "other_reasonable_adjustment_details")
    private String otherReasonableAdjustmentDetails;

    @Column(name = "custody_status")
    private String custodyStatus;

    @Column(name ="created_date_time")
    private LocalDateTime createdDateTime;

    @PrePersist
    public void prePersist() {
        createdDateTime = LocalDateTime.now();
    }

}
