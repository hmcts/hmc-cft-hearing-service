package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "contact_details")
@Entity
@Data
public class ContactDetailsEntity {

    @Column(name = "tech_party_id", nullable = false)
    private Long techPartyId;

    @Column(name = "contact_type")
    private String contactType;

    @Column(name = "contact_details")
    private String contactDetails;

}
