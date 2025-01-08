package uk.gov.hmcts.reform.hmc.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetHearingResponseTest {

    @Test
    void getPartyDetailsReturnsSortedByPartyID() {
        PartyDetails party1 = new PartyDetails();
        party1.setPartyID("2");

        PartyDetails party2 = new PartyDetails();
        party2.setPartyID("1");

        PartyDetails party3 = new PartyDetails();
        party3.setPartyID("3");

        GetHearingResponse response = new GetHearingResponse();
        response.setPartyDetails(Arrays.asList(party1, party2, party3));

        List<PartyDetails> sortedParties = response.getPartyDetails();

        assertThat(party2).isEqualTo(sortedParties.get(0));
        assertThat(party1).isEqualTo(sortedParties.get(1));
        assertThat(party3).isEqualTo(sortedParties.get(2));
    }

    @Test
    void getPartyDetailsReturnsEmptyListWhenEmpty() {
        GetHearingResponse response = new GetHearingResponse();
        response.setPartyDetails(Arrays.asList());

        List<PartyDetails> sortedParties = response.getPartyDetails();

        assertThat(sortedParties).isEmpty();
    }
}
