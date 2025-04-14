package uk.gov.hmcts.reform.hmc.helper;

import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class ElasticSearchQuery {

    private List<String> caseRefs;

    public String getQuery() {
        String joinedRefs = caseRefs.stream()
            .map(ref -> "\"" + ref + "\"")
            .collect(Collectors.joining(", "));

        return """
            {
                "query": {
                    "terms": {
                        "reference": [%s]
                    }
                },
                "_source": ["id", "jurisdiction", "case_type_id", "reference"]
            }
            """.formatted(joinedRefs);
    }
}

