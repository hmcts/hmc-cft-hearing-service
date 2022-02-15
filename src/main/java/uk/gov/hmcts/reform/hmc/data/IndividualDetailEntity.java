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

@Table(name = "individual_detail")
@Entity
@Data
@SecondaryTable(name = "hearing_party",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "TECH_PARTY_ID")})
public class IndividualDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "individual_detail_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "related_party_relationship_type", nullable = false)
    private String relatedPartyRelationshipType;

    @Column(name = "related_party_id", nullable = false)
    private String relatedPartyID;

    @Column(name = "vulnerability_details")
    private String vulnerabilityDetails;

    @Column(name = "vulnerable_flag")
    private Boolean vulnerableFlag;

    @Column(name = "interpreter_language")
    private String interpreterLanguage;

    @Column(name = "channel_type")
    private String channelType;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

}
