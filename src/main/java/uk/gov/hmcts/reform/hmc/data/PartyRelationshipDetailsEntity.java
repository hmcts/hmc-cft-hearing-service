package uk.gov.hmcts.reform.hmc.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
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

@Table(name = "party_relationship_details")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRelationshipDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "party_relationship_details_id_seq")
    @Column(name = "party_relationship_details_id")
    private Long partyRelationshipDetailsId;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "source_tech_party_id")
    private HearingPartyEntity sourceTechParty;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "target_tech_party_id")
    private HearingPartyEntity targetTechParty;

    @Column(name = "relationship_type")
    private String relationshipType;

    @PrePersist
    public void prePersist() {
        createdDateTime = LocalDateTime.now();
    }
}
