package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ElasticSearchQueryPaginatedTest {

    private static final String CASE_REF_1 = "1000100010001000";
    private static final String CASE_REF_2 = "2000200020002000";

    private static final String SEARCH_QUERY_JSON = """
        {
            "size": %d,
            "from": %d,
            "query": {
                "terms": {"reference": [%s]}
            },
            "sort": [
                {"reference.keyword": "asc"}
            ],
            "_source": ["id", "jurisdiction", "case_type_id", "reference"]
        }""";

    @ParameterizedTest(name = "{index}: {2}")
    @MethodSource("expectedSearchQueries")
    void shouldBuildElasticSearchQuery(Integer pageSize, Integer offset, List<String> caseReferences) {
        String query = ElasticSearchQueryPaginated.builder()
            .pageSize(pageSize)
            .offset(offset)
            .caseReferences(caseReferences)
            .build()
            .getQuery();

        String expectedQuery = createExpectedSearchQuery(pageSize, offset, caseReferences);
        assertElasticSearchQuery(expectedQuery, query);
    }

    @Test
    void shouldExcludeSizeParameter() {
        String query = ElasticSearchQueryPaginated.builder()
            .offset(5)
            .caseReferences(List.of(CASE_REF_1))
            .build()
            .getQuery();

        String expectedQuery = """
            {
                "from": 5,
                "query": {
                    "terms": {"reference": ["1000100010001000"]}
                },
                "sort": [
                    {"reference.keyword": "asc"}
                ],
                "_source": ["id", "jurisdiction", "case_type_id", "reference"]
            }""";
        assertElasticSearchQuery(expectedQuery, query);
    }

    @Test
    void shouldExcludeFromParameter() {
        String query = ElasticSearchQueryPaginated.builder()
            .pageSize(6)
            .caseReferences(List.of(CASE_REF_1))
            .build()
            .getQuery();

        String expectedQuery = """
            {
                "size": 6,
                "query": {
                    "terms": {"reference": ["1000100010001000"]}
                },
                "sort": [
                    {"reference.keyword": "asc"}
                ],
                "_source": ["id", "jurisdiction", "case_type_id", "reference"]
            }""";
        assertElasticSearchQuery(expectedQuery, query);
    }

    @Test
    void shouldExcludeSizeAndFromParameters() {
        String query = ElasticSearchQueryPaginated.builder()
            .caseReferences(List.of(CASE_REF_1))
            .build()
            .getQuery();

        String expectedQuery = """
            {
                "query": {
                    "terms": {"reference": ["1000100010001000"]}
                },
                "sort": [
                    {"reference.keyword": "asc"}
                ],
                "_source": ["id", "jurisdiction", "case_type_id", "reference"]
            }""";
        assertElasticSearchQuery(expectedQuery, query);
    }

    private static Stream<Arguments> expectedSearchQueries() {
        return Stream.of(
            arguments(1, 1, named("One case reference", List.of(CASE_REF_1))),
            arguments(2, 2, named("Two case references", List.of(CASE_REF_1, CASE_REF_2))),
            arguments(3, 3, named("Null case reference list", null)),
            arguments(4, 4, named("Empty case reference list", Collections.emptyList()))
        );
    }

    private String createExpectedSearchQuery(Integer pageSize, Integer offset, List<String> caseReferences) {
        String commaSeparatedCaseReferences =
            caseReferences == null || caseReferences.isEmpty() ? "" :
                "\"" + String.join("\", \"", caseReferences) + "\"";

        return SEARCH_QUERY_JSON.formatted(pageSize, offset, commaSeparatedCaseReferences);
    }

    private void assertElasticSearchQuery(String expectedQuery, String actualQuery) {
        assertEquals(expectedQuery, actualQuery, "Search query does not match expected query");
    }
}
