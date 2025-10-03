package uk.gov.hmcts.reform.hmc.helper;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.constants.Constants.ELASTIC_QUERY_DEFAULT_SIZE;

@Builder
@Data
public class ElasticSearchQuery {

    private List<String> caseRefs;

    private Integer size;

    public String getQuery() {
        String joinedRefs = caseRefs.stream()
            .map(ref -> "\"" + ref + "\"")
            .collect(Collectors.joining(", "));
        String sizeField = (caseRefs != null && caseRefs.size() > ELASTIC_QUERY_DEFAULT_SIZE)
            ? "\"size\": " + caseRefs.size() + ",\n"
            : "";
        return """
        {
          %s"query": {
            "terms": {
              "reference": [%s]
            }
          },
          "_source": ["id", "jurisdiction", "case_type_id", "reference"]
        }
        """.formatted(sizeField, joinedRefs);
    }
}

