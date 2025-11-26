package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.io.Serializable;
import java.util.List;

@Table(name = "actual_hearing_party")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ActualHearingPartyEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -7066394300898450286L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "actual_party_id_seq_generator")
    @SequenceGenerator(name = "actual_party_id_seq_generator", 
        sequenceName = "actual_party_id_seq", allocationSize = 1)
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

    @OneToMany(mappedBy = "sourceActualParty", cascade = CascadeType.PERSIST,  orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ActualPartyRelationshipDetailEntity> actualPartyRelationshipDetail;

    @OneToMany(mappedBy = "targetActualParty", cascade = CascadeType.PERSIST,  orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ActualPartyRelationshipDetailEntity> actualTargetPartyRelationshipDetail;

    @OneToOne(mappedBy = "actualHearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private ActualAttendeeIndividualDetailEntity actualAttendeeIndividualDetail;

}
