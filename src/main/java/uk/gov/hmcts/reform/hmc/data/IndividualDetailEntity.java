package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "individual_detail")
@Entity
@Data
public class IndividualDetailEntity {

    @Column(name = "tech_party_id", nullable = false)
    private Long techPartyId;

    @Column(name = "related_party_relationship_type", nullable = false)
    private String relatedPartyRelationshipType;

    @Column(name = "related_party_id", nullable = false)
    private String relatedPartyID;

    @Column(name = "vulnerability_details")
    private String vulnerabilityDetails;

    @Column(name = "vulnerable_flag")
    private boolean vulnerableFlag;

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

}
