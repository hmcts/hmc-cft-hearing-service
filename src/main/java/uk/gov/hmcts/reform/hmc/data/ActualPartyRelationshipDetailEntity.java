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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "actual_party_relationship_id_seq_generator")
    @SequenceGenerator(name = "actual_party_relationship_id_seq_generator", 
        sequenceName = "actual_party_relationship_id_seq", allocationSize = 1)
    @Column(name = "actual_party_relationship_id")
    private Long actualPartyRelationshipId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "target_actual_party_id")
    private ActualHearingPartyEntity targetActualParty;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "source_actual_party_id")
    private ActualHearingPartyEntity sourceActualParty;
}
