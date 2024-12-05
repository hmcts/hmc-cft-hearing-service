package uk.gov.hmcts.reform.hmc.client.datastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElasticSearch {

    private Query query;

}
