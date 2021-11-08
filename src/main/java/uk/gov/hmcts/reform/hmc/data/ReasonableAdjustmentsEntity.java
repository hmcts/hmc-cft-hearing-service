package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "reasonable_adjustments")
@Entity
@Data
public class ReasonableAdjustmentsEntity {

    @Column(name = "tech_party_id", nullable = false)
    private Long techPartyId;

    @Column(name = "reasonable_adjustment_code")
    private String reasonableAdjustmentCode;

}
