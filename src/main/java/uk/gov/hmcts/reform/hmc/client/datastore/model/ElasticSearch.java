package uk.gov.hmcts.reform.hmc.client.datastore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticSearch {

    private Query query;

    private Integer size;

}
