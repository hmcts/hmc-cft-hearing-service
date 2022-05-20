package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import java.io.Serializable;
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
import javax.persistence.Table;

@Table(name = "actual_attendee_individual_detail")
@Entity
@Data
public class ActualAttendeeIndividualDetailEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "actual_attendee_individual_detail_id_seq")
    @Column(name = "actual_attendee_individual_detail_id")
    private Long actualAttendeeIndividualDetailId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "party_organisation_name")
    private String partyOrganisationName;

    @Column(name = "party_actual_sub_channel_type", nullable = false)
    private String partyActualSubChannelType;

    @Column(name ="created_date_time")
    private LocalDateTime createdDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_party_id")
    private ActualHearingPartyEntity actualHearingParty;

    @PrePersist
    public void prePersist() {
        createdDateTime = LocalDateTime.now();
    }

}
