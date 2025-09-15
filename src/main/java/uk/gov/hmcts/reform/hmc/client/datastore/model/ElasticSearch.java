package uk.gov.hmcts.reform.hmc.client.datastore.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElasticSearch {

    private Query query;

    private Integer size;

    @Builder
    public ElasticSearch(Query query) {
        this.query = query;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Query getQuery() {
        return query;
    }

    public Integer getSize() {
        return size;
    }

}
