package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import org.hibernate.annotations.Type;
import uk.gov.hmcts.reform.hmc.model.CaseCategoryType;

import javax.persistence.*;

@Table(name = "case_categories")
@Entity
@Data
@SecondaryTable(name="CASE_HEARING_REQUEST",
    pkJoinColumns={
        @PrimaryKeyJoinColumn(name="CASE_HEARING_ID")})
public class CaseCategoriesEntity {

    @Id
    @SequenceGenerator(name="case_categories_id_seq",
        sequenceName="case_categories_id_seq",
        allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator="case_categories_id_seq")
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
