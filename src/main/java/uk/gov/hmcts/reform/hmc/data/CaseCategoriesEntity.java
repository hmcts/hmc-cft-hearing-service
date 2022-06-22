package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "case_categories")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class CaseCategoriesEntity extends BaseEntity implements Serializable, Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "case_categories_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "case_category_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
    @Enumerated(EnumType.STRING)
    private CaseCategoryType categoryType;

    @Column(name = "case_category_value")
    private String caseCategoryValue;

    @Column(name = "case_category_parent")
    private String caseCategoryParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
