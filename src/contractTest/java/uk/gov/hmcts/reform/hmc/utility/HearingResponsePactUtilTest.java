package uk.gov.hmcts.reform.hmc.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

class HearingResponsePactUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(HearingResponsePactUtilTest.class);

    @Test
    void addPanelMemberIds() {
        HearingResponsePactUtil.addPanelMemberIds();
    }

    @Test
    void addAttendees() {
        HearingResponsePactUtil.addAttendees();
    }

    @Test
    void addArrayListStrings() {
        List<String> panelMemberIds = new ArrayList<>();
        panelMemberIds.add("panelMemberId1");
        panelMemberIds.add("panelMemberId2");
        panelMemberIds.add("panelMemberId3");
        panelMemberIds.add("panelMemberId4");
        panelMemberIds.add("panelMemberId5");
        HearingResponsePactUtil.addArrayListStrings("panelMemberId",
                "^[a-zA-Z0-9]{1,60}$", panelMemberIds);
    }

    @Test
    void addHearingDaySchedules() {
        PactDslJsonArray pactDslJsonArray = HearingResponsePactUtil.addHearingDaySchedules();
        logger.info("pactDslJsonArray: {}", pactDslJsonArray);
        assertTrue(pactDslJsonArray.toString().contains("\"hearingStartDateTime\":"));
    }


    @Test
    void getGetHearingsJsonBody() {
        PactDslJsonBody pactDslJsonBody =
                HearingResponsePactUtil.generateGetHearingsJsonBody("testing 123",
                        "9372710950276233","LISTED");
        logger.info("pactDslJsonBody: {}", pactDslJsonBody);
        assertTrue(pactDslJsonBody.toString().contains("\"hearingDaySchedule\":"));
        assertTrue(pactDslJsonBody.toString().contains("\"attendees\":"));
        assertTrue(pactDslJsonBody.toString().contains("\"hearingStartDateTime\":"));
    }



}