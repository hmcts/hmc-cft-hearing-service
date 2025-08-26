package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Table(name = "contact_details")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "hearing_party",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "TECH_PARTY_ID")})
public class ContactDetailsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -4144280388835257685L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "contact_details_id_seq_generator")
    @SequenceGenerator(name = "contact_details_id_seq_generator", 
        sequenceName = "contact_details_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "contact_type")
    private String contactType;

    @Column(name = "contact_details")
    private String contactDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    public ContactDetailsEntity(ContactDetailsEntity original) {
        this.id = original.id;
        this.contactType = original.contactType;
        this.contactDetails = original.contactDetails;
        this.hearingParty = original.hearingParty;
    }
}
