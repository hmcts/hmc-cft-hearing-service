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

@Table(name = "party_relationship_details")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRelationshipDetailsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -4983833617462382058L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "party_relationship_details_id_seq_generator")
    @SequenceGenerator(name = "party_relationship_details_id_seq_generator", 
        sequenceName = "party_relationship_details_id_seq", allocationSize = 1)
    @Column(name = "party_relationship_details_id")
    private Long partyRelationshipDetailsId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "source_tech_party_id")
    private HearingPartyEntity sourceTechParty;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "target_tech_party_id")
    private HearingPartyEntity targetTechParty;

    @Column(name = "relationship_type")
    private String relationshipType;

    public PartyRelationshipDetailsEntity(PartyRelationshipDetailsEntity original) {
        this.partyRelationshipDetailsId = original.partyRelationshipDetailsId;
        this.sourceTechParty = original.sourceTechParty;
        this.targetTechParty = original.targetTechParty;
        this.relationshipType = original.relationshipType;
    }
}
