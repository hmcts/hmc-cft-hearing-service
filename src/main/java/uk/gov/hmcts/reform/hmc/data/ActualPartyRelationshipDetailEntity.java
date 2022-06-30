package uk.gov.hmcts.reform.hmc.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name = "actual_party_relationship_detail")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActualPartyRelationshipDetailEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 7682399478560625882L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "actual_party_relationship_id_seq")
    @Column(name = "actual_party_relationship_id")
    private Long actualPartyRelationshipId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "target_actual_party_id")
    private ActualHearingPartyEntity targetActualParty;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "source_actual_party_id")
    private ActualHearingPartyEntity sourceActualParty;
}
