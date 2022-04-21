package uk.gov.hmcts.reform.hmc.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "party_relationship_details")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRelationshipDetailsEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
            generator = "party_relationship_details_id_seq")
    @Column(name = "party_relationship_details_id")
    private Long partyRelationshipDetailsId;

    @Column(name = "source_tech_party_id")
    private Long sourceTechPartyId;

    @Column(name = "target_tech_party_id")
    private Long targetTechPartyId;

    @Column(name = "relationship_type")
    private String relationshipType;
}