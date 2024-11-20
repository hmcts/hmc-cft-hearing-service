package uk.gov.hmcts.reform.hmc.client.datastore.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CaseSearchResult {

    private Long total;
    private List<DataStoreCaseDetails> cases;

}
