package uk.gov.hmcts.reform.hmc.client.datastore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseSearchResult {

    private List<DataStoreCaseDetails> cases;

}
