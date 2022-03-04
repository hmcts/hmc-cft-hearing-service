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

@Table(name = "actual_party_relationship_detail")
@Entity
@Data
@SecondaryTable(name = "ACTUAL_HEARING_PARTY",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "actual_party_id)")})
public class ActualPartyRelationshipDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "actual_party_relationship_id_seq")
    @Column(name = "actual_party_relationship_id")
    private Long actualPartyRelationshipId;

    @Column(name = "target_actual_party_id")
    private Long targetActualPartyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_actual_party_id")
    private ActualHearingPartyEntity actualHearingParty;
}
