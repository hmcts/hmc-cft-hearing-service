package uk.gov.hmcts.reform.hmc.client.datastore;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseDetailsTest {

    private ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

    @Test
    void givenTwoJsonFormatsForCaseIdWhenDeserialisedThenCaseDetailsObjectsCreated() throws Exception {

        DataStoreCaseDetails caseDetails = objectMapper.readValue("{\n"
                                                                      + "  \"id\": \"12345\",\n"
                                                                      + "  \"jurisdiction\": \"CMC\"\n"
                                                                      + "}", DataStoreCaseDetails.class);

        assertEquals("12345", caseDetails.getId());
        assertEquals("CMC", caseDetails.getJurisdiction());

        caseDetails = objectMapper.readValue("{\n"
                                                 + "  \"reference\": \"12345\",\n"
                                                 + "  \"jurisdiction\": \"CMC\"\n"
                                                 + "}", DataStoreCaseDetails.class);

        assertEquals("12345", caseDetails.getId());
        assertEquals("CMC", caseDetails.getJurisdiction());
    }

    @Test
    void shouldSerialiseToCaseIdAlways() throws Exception {

        DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
            .id("12345")
            .jurisdiction("CMC")
            .build();

        String json = objectMapper.writeValueAsString(caseDetails);

        org.skyscreamer.jsonassert.JSONAssert.assertEquals(
            "{\"jurisdiction\":\"CMC\",\"id\":\"12345\"}",
            json,
            JSONCompareMode.LENIENT
        );
    }
}
