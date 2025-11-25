package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Table(name = "actual_attendee_individual_detail")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ActualAttendeeIndividualDetailEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 6287971344884542654L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "actual_attendee_individual_detail_id_seq_generator")
    @SequenceGenerator(name = "actual_attendee_individual_detail_id_seq_generator", 
        sequenceName = "actual_attendee_individual_detail_id_seq", allocationSize = 1)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_party_id")
    private ActualHearingPartyEntity actualHearingParty;


}
