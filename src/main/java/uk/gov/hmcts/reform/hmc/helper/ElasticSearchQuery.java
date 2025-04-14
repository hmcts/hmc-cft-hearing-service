package uk.gov.hmcts.reform.hmc.helper;

import lombok.Builder;

@Builder
public class ElasticSearchQuery {

    private static final String QUERY = """
        {
            "query": {
                "terms": {
                    "reference": [
                        "1733244309444665",
                        "1733225862820847",
                        "1733233844992551"
                    ]
                }
            },
            "_source": ["id", "jurisdiction", "case_type_id", "reference"]
        }
        """;

    public String getQuery() {
        return QUERY;
    }

}
