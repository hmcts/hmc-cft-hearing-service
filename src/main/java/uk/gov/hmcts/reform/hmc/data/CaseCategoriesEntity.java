package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "case_categories")
@Entity
@Data
public class CaseCategoriesEntity {

    @Column(name = "case_hearing_id", nullable = false)
    private Long caseHearingID;

    @Column(name = "case_category_type", nullable = false)
    private CaseCategoryType locationId;

    @Column(name = "case_category_value")
    private String caseCategoryValue;

}
