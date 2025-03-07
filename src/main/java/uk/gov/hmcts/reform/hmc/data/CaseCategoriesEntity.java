package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;

import java.io.Serializable;

@Table(name = "case_categories")
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class CaseCategoriesEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3631552987002525237L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
        generator = "case_categories_id_seq_generator")
    @SequenceGenerator(name = "case_categories_id_seq_generator", 
        sequenceName = "case_categories_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "case_category_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CaseCategoryType categoryType;

    @Column(name = "case_category_value")
    private String caseCategoryValue;

    @Column(name = "case_category_parent")
    private String caseCategoryParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

    public CaseCategoriesEntity(CaseCategoriesEntity original) {
        this.id = original.id;
        this.categoryType = original.categoryType;
        this.caseCategoryValue = original.caseCategoryValue;
        this.caseCategoryParent = original.caseCategoryParent;
        this.caseHearing = original.caseHearing;
    }
}
