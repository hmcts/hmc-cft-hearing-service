package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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

@Table(name = "reasonable_adjustments")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "hearing_party",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "TECH_PARTY_ID")})
public class ReasonableAdjustmentsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 6304356931641668467L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "reasonable_adjustments_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "reasonable_adjustment_code")
    private String reasonableAdjustmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_party_id")
    private HearingPartyEntity hearingParty;

    public ReasonableAdjustmentsEntity(ReasonableAdjustmentsEntity original) {
        this.id = original.id;
        this.reasonableAdjustmentCode = original.reasonableAdjustmentCode;
        this.hearingParty = original.hearingParty;
    }
}
