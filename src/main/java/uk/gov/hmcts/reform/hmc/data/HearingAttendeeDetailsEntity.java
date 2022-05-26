package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing_attendee_details")
@Entity
@Data
@SecondaryTable(name = "HEARING_DAY_DETAILS",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_day_id")})
public class HearingAttendeeDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_day_panel_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "party_id")
    private String partyId;

    @Column(name = "party_sub_channel_type", nullable = false)
    private String partySubChannelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_day_id")
    private HearingDayDetailsEntity hearingDayDetails;

}
