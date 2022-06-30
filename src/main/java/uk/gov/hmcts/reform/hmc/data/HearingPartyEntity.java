package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing_party")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class HearingPartyEntity extends BaseEntity implements Serializable, Cloneable {

    private static final long serialVersionUID = -1378995263864233869L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "tech_party_id_seq")
    @Column(name = "tech_party_id", nullable = false)
    private Long techPartyId;

    @Column(name = "party_reference", nullable = false)
    private String partyReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
    private PartyType partyType;

    @Column(name = "party_role_type")
    private String partyRoleType;

    @OneToOne(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private IndividualDetailEntity individualDetailEntity;

    @OneToOne(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private OrganisationDetailEntity organisationDetailEntity;

    @OneToMany(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<UnavailabilityEntity> unavailabilityEntity;

    @OneToMany(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ContactDetailsEntity> contactDetailsEntity;

    @OneToMany(mappedBy = "hearingParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ReasonableAdjustmentsEntity> reasonableAdjustmentsEntity;

    @OneToMany(mappedBy = "sourceTechParty", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PartyRelationshipDetailsEntity> partyRelationshipDetailsEntity;

    public HearingPartyEntity(HearingPartyEntity original) {
        this.caseHearing = original.caseHearing;
        this.techPartyId = original.techPartyId;
        this.partyReference = original.partyReference;
        this.partyType = original.partyType;
        this.partyRoleType = original.partyRoleType;
        this.individualDetailEntity =  original.individualDetailEntity;
        this.organisationDetailEntity =  original.organisationDetailEntity;
        this.unavailabilityEntity =  original.unavailabilityEntity;
        this.contactDetailsEntity =  original.contactDetailsEntity;
        this.reasonableAdjustmentsEntity =  original.reasonableAdjustmentsEntity;
        this.partyRelationshipDetailsEntity =  original.partyRelationshipDetailsEntity;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        HearingPartyEntity cloned = (HearingPartyEntity) super.clone();
        cloneUnavailability(cloned);
        cloneContactDetails(cloned);
        cloneReasonableAdjustments(cloned);
        cloneIndividualDetails(cloned);
        cloneOrganisationDetails(cloned);
        clonePartyRelationshipDetails(cloned);
        cloned.setTechPartyId(null);
        return cloned;
    }

    private void cloneIndividualDetails(HearingPartyEntity cloned) {
        //IndividualDetailEntity
        if (null != cloned.getIndividualDetailEntity()) {
            IndividualDetailEntity clonedSubValue = new IndividualDetailEntity(cloned.getIndividualDetailEntity());
            clonedSubValue.setId(null);
            clonedSubValue.setHearingParty(cloned);
            cloned.setIndividualDetailEntity(clonedSubValue);
        }
    }

    private void cloneOrganisationDetails(HearingPartyEntity cloned) {
        //OrganisationDetailEntity
        if (null != cloned.getOrganisationDetailEntity()) {
            OrganisationDetailEntity clonedSubValue =
                 new OrganisationDetailEntity(cloned.getOrganisationDetailEntity());
            clonedSubValue.setId(null);
            clonedSubValue.setHearingParty(cloned);
            cloned.setOrganisationDetailEntity(clonedSubValue);
        }
    }

    private void cloneUnavailability(HearingPartyEntity cloned) {
        //UnavailabilityEntity
        List<UnavailabilityEntity> unavailabilityEntityList = new ArrayList<>();
        if (null != cloned.getUnavailabilityEntity()) {
            for (UnavailabilityEntity ue : cloned.getUnavailabilityEntity()) {
                UnavailabilityEntity clonedSubValue = new UnavailabilityEntity(ue);
                clonedSubValue.setId(null);
                clonedSubValue.setHearingParty(cloned);
                unavailabilityEntityList.add(clonedSubValue);
            }
        }
        cloned.setUnavailabilityEntity(unavailabilityEntityList);
    }

    private void cloneContactDetails(HearingPartyEntity cloned) {
        //ContactDetailsEntity
        List<ContactDetailsEntity> contactDetailsEntityList = new ArrayList<>();
        if (null != cloned.getContactDetailsEntity()) {
            for (ContactDetailsEntity cde : cloned.getContactDetailsEntity()) {
                ContactDetailsEntity clonedSubValue =  new ContactDetailsEntity(cde);
                clonedSubValue.setId(null);
                clonedSubValue.setHearingParty(cloned);
                contactDetailsEntityList.add(clonedSubValue);
            }
        }
        cloned.setContactDetailsEntity(contactDetailsEntityList);
    }

    private void cloneReasonableAdjustments(HearingPartyEntity cloned) {
        //ReasonableAdjustmentsEntity
        List<ReasonableAdjustmentsEntity> reasonableAdjustmentsEntityList = new ArrayList<>();
        if (null != cloned.getReasonableAdjustmentsEntity()) {
            for (ReasonableAdjustmentsEntity rae : cloned.getReasonableAdjustmentsEntity()) {
                ReasonableAdjustmentsEntity clonedSubValue = new ReasonableAdjustmentsEntity(rae);
                clonedSubValue.setId(null);
                clonedSubValue.setHearingParty(cloned);
                reasonableAdjustmentsEntityList.add(clonedSubValue);
            }
        }
        cloned.setReasonableAdjustmentsEntity(reasonableAdjustmentsEntityList);
    }

    private void clonePartyRelationshipDetails(HearingPartyEntity cloned) {
        //PartyRelationshipDetailsEntity
        List<PartyRelationshipDetailsEntity> partyRelationshipDetailsEntityList = new ArrayList<>();
        if (null != cloned.getPartyRelationshipDetailsEntity()) {
            for (PartyRelationshipDetailsEntity prde : cloned.getPartyRelationshipDetailsEntity()) {
                PartyRelationshipDetailsEntity clonedSubValue = new PartyRelationshipDetailsEntity(prde);
                clonedSubValue.setPartyRelationshipDetailsId(null);
                clonedSubValue.setSourceTechParty(cloned);
                partyRelationshipDetailsEntityList.add(clonedSubValue);
            }
        }
        cloned.setPartyRelationshipDetailsEntity(partyRelationshipDetailsEntityList);
    }
}
