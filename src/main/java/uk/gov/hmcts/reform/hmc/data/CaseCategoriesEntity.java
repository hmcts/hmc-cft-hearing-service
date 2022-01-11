package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "case_categories")
@Entity
@Data
@SecondaryTable(name = "CASE_HEARING_REQUEST",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "CASE_HEARING_ID")})
public class CaseCategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "case_categories_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "case_category_type", nullable = false)
    @Type(type = "uk.gov.hmcts.reform.hmc.model.PostgresEnumType")
    private CaseCategoryType categoryType;

    @Column(name = "case_category_value")
    private String caseCategoryValue;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_hearing_id")
    private CaseHearingRequestEntity caseHearing;

}
