package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.List;
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

@Table(name = "actual_hearing_party")
@Entity
@Data
@SecondaryTable(name = "ACTUAL_HEARING_DAY",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "actual_hearing_day_id")})
public class ActualHearingPartyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "actual_party_id_seq")
    @Column(name = "actual_party_id")
    private Long actualPartyId;

    @Column(name = "party_id")
    private String partyId;

    @Column(name = "actual_party_role_type", nullable = false)
    private String actualPartyRoleType;

    @Column(name = "did_not_attend_flag")
    private Boolean didNotAttendFlag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_hearing_day_id")
    private ActualHearingDayEntity actualHearingDay;

    @OneToMany(mappedBy = "actualHearingParty")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ActualPartyRelationshipDetailEntity> actualPartyRelationshipDetail;

    @OneToMany(mappedBy = "actualHearingParty")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ActualAttendeeIndividualDetailEntity> actualAttendeeIndividualDetail;
}
