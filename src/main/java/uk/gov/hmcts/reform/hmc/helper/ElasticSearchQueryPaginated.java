package uk.gov.hmcts.reform.hmc.helper;

import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class ElasticSearchQueryPaginated {

    private static final String PARAM_SIZE = "size";
    private static final String PARAM_FROM = "from";

    private Integer pageSize;
    private Integer offset;
    private List<String> caseReferences;

    public String getQuery() {
        String paginationParams =
            createPaginationParam(PARAM_SIZE, pageSize) + createPaginationParam(PARAM_FROM, offset);

        String commaSeparatedCaseReferences =
            caseReferences == null ? "" :
                caseReferences.stream().map(caseRef -> "\"" + caseRef + "\"").collect(Collectors.joining(", "));

        return """
            {
                %s"query": {
                    "terms": {"reference": [%s]}
                },
                "sort": [
                    {"reference.keyword": "asc"}
                ],
                "_source": ["id", "jurisdiction", "case_type_id", "reference"]
            }""".formatted(paginationParams, commaSeparatedCaseReferences);
    }

    private String createPaginationParam(String paramName, Integer paramValue) {
        // Spaces after newline are to preserve indentation
        return paramValue == null ? "" : "\"" + paramName + "\": " + paramValue + ",\n    ";
    }
}
